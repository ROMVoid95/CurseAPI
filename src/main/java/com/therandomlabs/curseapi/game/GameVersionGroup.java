package com.therandomlabs.curseapi.game;

import java.util.Locale;
import com.therandomlabs.utils.collection.TRLList;

public interface GameVersionGroup<G extends GameVersionGroup> extends Comparable<G> {
	default String id() {
		return toString().toLowerCase(Locale.ENGLISH);
	}

	TRLList<? extends GameVersion> getVersions();

	default boolean newerThan(G group) {
		return compareTo(group) > 0;
	}

	default boolean newerThanOrEqualTo(G group) {
		return compareTo(group) >= 0;
	}

	default boolean olderThan(G group) {
		return compareTo(group) < 0;
	}

	default boolean olderThanOrEqualTo(G group) {
		return compareTo(group) <= 0;
	}
}