package alice.tuprolog.event;

import alice.tuprolog.lib.UserContextInputStream;

import java.util.EventObject;

public class ReadEvent extends EventObject {

	/**
	 * 
	 */
	private final UserContextInputStream stream;
	
	public ReadEvent(UserContextInputStream str) {
		super(str);
		this.stream = str;
	}

	public UserContextInputStream getStream()
	{
		return this.stream;
	}

}