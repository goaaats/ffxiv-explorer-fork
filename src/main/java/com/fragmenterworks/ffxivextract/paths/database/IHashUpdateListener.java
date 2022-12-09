package com.fragmenterworks.ffxivextract.paths.database;

import com.fragmenterworks.ffxivextract.paths.CrcResult;

import java.util.List;

public interface IHashUpdateListener {
	void onHashUpdate(HashUpdateNotification notification);
	void onMultipleHashUpdate(List<HashUpdateNotification> notifications);
}
