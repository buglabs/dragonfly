package com.buglabs.bug.module.motion.pub;

public class InputEvent {
	public TimeVal time;
	public int type;
	public int code;
	public long value;

	public final static byte EV_SYN = 0x00;
	public final static byte EV_KEY	= 0x01;
	public final static byte EV_REL	= 0x02;
	public final static byte EV_ABS	= 0x03;
	public final static byte EV_MSC	= 0x04;
	public final static byte EV_SW	= 0x05;
	public final static byte EV_LED	= 0x11;
	public final static byte EV_SND	= 0x12;
	public final static byte EV_REP	= 0x14;
	public final static byte EV_FF	= 0x15;
	public final static byte EV_PWR	= 0x16;
	public final static byte EV_FF_STATUS = 0x17;
	public final static byte EV_MAX = 0x1f;
	
}
