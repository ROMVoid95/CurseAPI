package com.therandomlabs.curseapi;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;

import com.google.common.base.Preconditions;

public class CurseFiles extends TreeSet<CurseFile> {
	public static final Comparator<CurseFile> SORT_BY_NEWEST = CurseFile::compareTo;
	public static final Comparator<CurseFile> SORT_BY_OLDEST = SORT_BY_NEWEST.reversed();

	private static final long serialVersionUID = -7609834501394579694L;

	/**
	 * Creates an empty {@link CurseFiles} instance ordered from newest to oldest.
	 */
	public CurseFiles() {
		//Default constructor.
	}

	public CurseFiles(Comparator<? super CurseFile> comparator) {
		super(comparator);
	}

	public CurseFiles(Collection<? extends CurseFile> files) {
		super(files);
	}

	public CurseFiles(
			Collection<? extends CurseFile> files, Comparator<? super CurseFile> comparator
	) {
		super(comparator);
		addAll(files);
	}

	@Override
	public CurseFiles clone() {
		return (CurseFiles) super.clone();
	}

	public Optional<CurseFile> fileWithID(int id) {
		Preconditions.checkArgument(id >= 10, "id should not be smaller than 10");

		for (CurseFile file : this) {
			if (file.id() == file.id()) {
				return Optional.of(file);
			}
		}

		return Optional.empty();
	}

	public CurseFiles withComparator(Comparator<? super CurseFile> comparator) {
		return new CurseFiles(this, comparator);
	}
}
