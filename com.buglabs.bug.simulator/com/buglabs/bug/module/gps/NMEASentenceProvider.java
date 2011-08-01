/*******************************************************************************
 * Copyright (c) 2008, 2009 Bug Labs, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package com.buglabs.bug.module.gps;

import java.io.BufferedReader;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.measurement.Measurement;
import org.osgi.util.measurement.Unit;
import org.osgi.util.position.Position;

import com.buglabs.bug.module.gps.pub.INMEASentenceProvider;
import com.buglabs.bug.module.gps.pub.INMEASentenceSubscriber;
import com.buglabs.bug.module.gps.pub.IPositionSubscriber;
import com.buglabs.nmea.sentences.NMEAParserException;
import com.buglabs.nmea2.AbstractNMEASentence;
import com.buglabs.nmea2.NMEASentenceFactory;
import com.buglabs.nmea2.RMC;
import com.buglabs.util.osgi.LogServiceUtil;

/**
 * This class is a thread that listens for NMEA sentences on the InputStream
 * passed in the constructor, and will present the last parsed sentence to
 * clients.
 * 
 * @author aroman
 * 
 */
public class NMEASentenceProvider extends Thread implements INMEASentenceProvider, ServiceListener {

	private static final int MAX_LOG_FILE_SIZE = 32000;

	private InputStream nmeaStream;

	private RMC cachedRMC;

	private LogService log = null;
	private volatile int index = 0;
	private List subscribers;

	private final BundleContext context;

	private final int samplingRate;

	public NMEASentenceProvider(int samplingRate, InputStream gpsInputStream, BundleContext context) {
		this.samplingRate = samplingRate;
		nmeaStream = gpsInputStream;
		this.context = context;
		log = LogServiceUtil.getLogService(context);

		try {
			subscribers = loadPreregisteredSubscribers(context);
		} catch (InvalidSyntaxException e) {
			//Since we are not passing a Filter, this exception can be safely ignored.
		}
	}

	private List loadPreregisteredSubscribers(BundleContext context2) throws InvalidSyntaxException {
		ServiceReference[] sr = context2.getServiceReferences(INMEASentenceSubscriber.class.getName(), null);
		List sub = null;

		if (sr != null && sr.length > 0) {
			sub = new ArrayList();
			for (int i = 0; i < sr.length; ++i) {
				sub.add(context2.getService(sr[i]));
			}
		}

		return sub;
	}

	/* (non-Javadoc)
	 * @see com.buglabs.bug.module.gps.pub.INMEASentenceProvider#getRMC()
	 * @deprecated
	 */
	public com.buglabs.nmea.sentences.RMC getRMC() {
		// Only return RMC if valid position information is available.
		if (cachedRMC == null || cachedRMC.getLatitude() == null || cachedRMC.getLongitude() == null) {
			return null;
		}

		return new com.buglabs.nmea.sentences.RMC(cachedRMC);
	}

	public RMC getLastRMC() {
		return cachedRMC;
	}

	public void run() {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(nmeaStream));
			br.mark(MAX_LOG_FILE_SIZE);
			String sentence;

			do {
				sleep(samplingRate);
				try {
					sentence = br.readLine();
				} catch (CharConversionException e) {
					sentence = "";
					continue;
				}

				//Reset the input data to the beginging.
				if (sentence == null) {
					br.reset();
					continue;
				}

				try {
					AbstractNMEASentence objSentence = NMEASentenceFactory.getSentence(sentence);

					if (objSentence != null && objSentence instanceof RMC) {
						cachedRMC = (RMC) objSentence;
						index++;
					}
					if (objSentence != null) {
						notifySubscribers(objSentence);
					}
				} catch (NMEAParserException e) {
					log.log(LogService.LOG_WARNING, "An error occured while parsing sentence.", e);
				}
			} while (!Thread.currentThread().isInterrupted() && (sentence != null));
		} catch (IOException e) {
			log.log(LogService.LOG_ERROR, "An IO Error occurred in reading from NMEA stream.", e);
		} catch (InterruptedException e) {

		} finally {
			try {
				if (br != null) {
					br.close();
				} else {
					if (nmeaStream != null) {
						nmeaStream.close();
					}
				}
			} catch (IOException e) {
			}
		}
	}

	private void notifySubscribers(AbstractNMEASentence objSentence) {
		if (subscribers == null || subscribers.size() == 0) {
			return;
		}

		synchronized (subscribers) {
			for (Iterator i = subscribers.iterator(); i.hasNext();) {
				Object subscriber = i.next();

				if (subscriber instanceof INMEASentenceSubscriber) {
					INMEASentenceSubscriber sub = (INMEASentenceSubscriber) subscriber;
					sub.sentenceReceived(objSentence);
				} else if (subscriber instanceof IPositionSubscriber && objSentence instanceof RMC) {
					IPositionSubscriber sub = (IPositionSubscriber) subscriber;
					sub.positionUpdate(calculatePosition((RMC) objSentence));
				}
			}
		}
	}

	private Position calculatePosition(RMC rmc) {
		return new Position(new Measurement(rmc.getLatitudeAsDMS().toDecimalDegrees() * Math.PI / 180.0, Unit.rad), new Measurement(rmc.getLongitudeAsDMS().toDecimalDegrees()
				* Math.PI / 180.0, Unit.rad), new Measurement(0.0d, Unit.m), null, null);
	}

	public int getIndex() {
		return index;
	}

	public void serviceChanged(ServiceEvent event) {

		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
			if (subscribers == null) {
				subscribers = new ArrayList();
			}
			subscribers.add(context.getService(event.getServiceReference()));
			break;
		case ServiceEvent.UNREGISTERING:
			if (subscribers != null) {
				subscribers.remove(context.getService(event.getServiceReference()));
			}
			break;
		}
	}
}
