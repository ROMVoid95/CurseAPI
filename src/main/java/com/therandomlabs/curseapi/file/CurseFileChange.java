package com.therandomlabs.curseapi.file;

import com.google.common.base.Preconditions;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;

/**
 * Represents a change between two CurseForge file versions, an old file and a new file.
 * If a {@link CurseFileChange} represents a downgrade, the new file is older than the old file.
 *
 * @param <F> the type of {@link BasicCurseFile}.
 */
public class CurseFileChange<F extends BasicCurseFile> {
	private final F oldFile;
	private final F newFile;

	/**
	 * Constructs a {@link CurseFileChange} with the specified old file and new file.
	 * If this {@link CurseFileChange} is to represent a downgrade, the new file may be
	 * older than the old file.
	 *
	 * @param oldFile an old file.
	 * @param newFile a new file.
	 */
	protected CurseFileChange(F oldFile, F newFile) {
		Preconditions.checkNotNull(oldFile, "oldFile should not be null");
		Preconditions.checkNotNull(newFile, "newFile should not be null");
		Preconditions.checkArgument(
				oldFile.sameProject(newFile),
				"oldFile and newFile should belong to the same project"
		);
		Preconditions.checkArgument(
				!oldFile.equals(newFile), "oldFile and newFile should represent different files"
		);
		this.oldFile = oldFile;
		this.newFile = newFile;
	}

	/**
	 * Returns the project ID of the old and new files.
	 *
	 * @return the project ID of the old and new files.
	 */
	public int projectID() {
		return oldFile.projectID();
	}

	/**
	 * Returns the project of the old and new files as a {@link CurseProject}.
	 *
	 * @return the project of the old and new files as a {@link CurseProject}.
	 * @throws CurseException if an error occurs.
	 */
	public CurseProject project() throws CurseException {
		return oldFile.project();
	}

	/**
	 * Returns the old file. This is not necessarily older than the new file.
	 *
	 * @return the old file.
	 */
	public F oldFile() {
		return oldFile;
	}

	/**
	 * Returns the new file. This is not necessarily newer than the old file.
	 *
	 * @return the new file.
	 */
	public F newFile() {
		return newFile;
	}

	/**
	 * Returns the older file.
	 * This may refer to the new file if this {@link CurseFileChange} represents a downgrade.
	 *
	 * @return the older file.
	 */
	public F olderFile() {
		return isDowngrade() ? newFile : oldFile;
	}

	/**
	 * Returns the newer file.
	 * This may refer to the old file if this {@link CurseFileChange} represents a downgrade.
	 *
	 * @return the newer file.
	 */
	public F newerFile() {
		return isDowngrade() ? oldFile : newFile;
	}

	/**
	 * Returns whether this {@link CurseFileChange} represents a downgrade.
	 *
	 * @return {@code true} if the file returned by {@link #oldFile()} is newer than
	 * the file returned by {@link #newFile()}, or otherwise {@code false}.
	 */
	public boolean isDowngrade() {
		return oldFile.newerThan(newFile);
	}
}