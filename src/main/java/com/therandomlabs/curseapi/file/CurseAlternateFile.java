/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2020 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.curseapi.file;

import java.util.Optional;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import okhttp3.HttpUrl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jsoup.nodes.Element;

/**
 * Represents an alternate file on CurseForge.
 * <p>
 * Implementations of this class should be effectively immutable.
 */
public abstract class CurseAlternateFile extends BasicCurseFile implements ExistingCurseFile {
	//Cache.
	@Nullable
	private transient CurseProject project;
	@Nullable
	private transient HttpUrl downloadURL;
	@Nullable
	private transient Element changelog;
	@Nullable
	private transient CurseFile mainFile;

	/**
	 * {@inheritDoc}
	 */
	@NonNull
	@Override
	public CurseProject project() throws CurseException {
		if (project == null) {
			final Optional<CurseProject> optionalProject = CurseAPI.project(projectID());

			if (!optionalProject.isPresent()) {
				throw new CurseException("Failed to retrieve CurseProject: " + this);
			}

			project = optionalProject.get();
		}

		return project;
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull
	@Override
	public CurseProject refreshProject() throws CurseException {
		project = null;
		return project();
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull
	@Override
	public HttpUrl url() throws CurseException {
		return project().fileURL(id());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpUrl downloadURL() throws CurseException {
		if (downloadURL == null) {
			final Optional<HttpUrl> optionalDownloadURL =
					CurseAPI.fileDownloadURL(projectID(), id());

			if (!optionalDownloadURL.isPresent()) {
				throw new CurseException("Failed to retrieve download URL: " + this);
			}

			downloadURL = optionalDownloadURL.get();
		}

		return downloadURL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpUrl refreshDownloadURL() throws CurseException {
		downloadURL = null;
		return downloadURL();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element changelog() throws CurseException {
		if (changelog == null) {
			final Optional<Element> optionalChangelog = CurseAPI.fileChangelog(projectID(), id());

			if (!optionalChangelog.isPresent()) {
				throw new CurseException("Failed to retrieve changelog: " + this);
			}

			changelog = optionalChangelog.get();
		}

		return changelog;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element refreshChangelog() throws CurseException {
		changelog = null;
		return changelog();
	}

	/**
	 * Returns the ID of this alternate file's main file.
	 *
	 * @return the ID of this alternate file's main file.
	 */
	public abstract int mainFileID();

	/**
	 * Returns this alternate file's main file.
	 * If this {@link CurseAlternateFile} implementation caches this value,
	 * it may be refreshed by calling {@link #refreshMainFile()}.
	 *
	 * @return this alternate file's main file as a {@link CurseFile}.
	 * @throws CurseException if an error occurs.
	 */
	public CurseFile mainFile() throws CurseException {
		if (mainFile == null) {
			final Optional<CurseFile> optionalFile = CurseAPI.file(projectID(), mainFileID());

			if (!optionalFile.isPresent()) {
				throw new CurseException("Failed to retrieve main file as CurseFile: " + this);
			}

			mainFile = optionalFile.get();
		}

		return mainFile;
	}

	/**
	 * If this {@link CurseAlternateFile} implementation caches the value returned by
	 * {@link #mainFile()}, this method refreshes this value and returns it.
	 *
	 * @return the refreshed value returned by {@link #mainFile()}.
	 * @throws CurseException if an error occurs.
	 */
	public CurseFile refreshMainFile() throws CurseException {
		mainFile = null;
		return mainFile();
	}
}
