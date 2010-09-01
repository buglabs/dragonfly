package com.buglabs.bug.module.audio.pub;

import com.buglabs.device.IButtonEventProvider;




/**
 * An input event provider for the Audio Module.  The audio module is different from
 * others in that the same device node used for button events is used also for events
 * related to jack insertion/removal.  
 * 
 * The events upon removal are rather difficult to discern though.  The button release
 * event for either VOL_UP or VOL_DOWN identical in getSource(), getAction(), getButton(), and
 * getRawValue().  The same is true for the removal events for jacks.  Jack removal and button release
 * events are identical.  Need to rework the driver to more easily discern these events.
 * 
 * @see com.buglabs.device.ButtonEvent, linux-bug_2.6.27.2/drivers/bmi/pims/bmi_audio.c
 * for more information.
 * 
 * @author jconnolly
  * @deprecated This module is not supported in BUG 2.0
 * 
 */
public interface IAudioEventProvider extends IButtonEventProvider {
	
	public static final int HEADPHONE_INSERTED_ACTION = 1;
	
	public static final int MIC_INSERTED_ACTION = 2;
	
	public static final int LINEOUT_INSERTED_ACTION = 4;
	
	public static final int LINEIN_INSERTED_ACTION = 8;
	
	public static final int BUTTON_AUDIO_VOLDOWN_PRESSED_ACTION = 16;
	
	public static final int BUTTON_AUDIO_VOLUP_PRESSED_ACTION = 32;

	public static final int BUTTON_AUDIO = 40;
	
	public static final int BUTTON_RELEASED_ACTION = 0;
	
	public static final int JACK_REMOVED_ACTION = 0;

}

