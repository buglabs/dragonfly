package com.buglabs.dragonfly.ui.actions;

public interface IImportQuery {	
	public static final int CANCEL = 0;
	public static final int NO = 1;
	public static final int YES = 2;

	int doQuery(String message);
}
