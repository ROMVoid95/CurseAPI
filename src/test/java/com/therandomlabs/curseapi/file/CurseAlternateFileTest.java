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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CurseAlternateFileTest {
	private static CurseAlternateFile file;

	@Test
	void projectIDShouldBeValid() {
		assertThat(file.projectID()).isGreaterThanOrEqualTo(CurseAPI.MIN_PROJECT_ID);
	}

	@Test
	void projectShouldBeValid() throws CurseException {
		final CurseProject project = file.project();
		assertThat(project).isNotNull().isEqualTo(file.refreshProject());
		assertThat(project.id()).isEqualTo(file.projectID());
	}

	@Test
	void idShouldBeValid() {
		assertThat(file.id()).isGreaterThanOrEqualTo(CurseAPI.MIN_FILE_ID);
	}

	@Test
	void urlShouldNotBeNull() throws CurseException {
		assertThat(file.url()).isNotNull();
	}

	@Test
	void toCurseFileShouldReturnNull() throws CurseException {
		assertThat(file.toCurseFile()).isNull();
	}

	@Test
	void downloadURLShouldNotBeNull() throws CurseException {
		assertThat(file.downloadURL()).isNotNull().isEqualTo(file.refreshDownloadURL());
	}

	@Test
	void changelogPlainTextShouldBeValid() throws CurseException {
		final String changelog = file.changelogPlainText(10);
		assertThat(changelog).isNotNull();

		file.refreshChangelog();
		assertThat(file.changelogPlainText(10)).isEqualTo(changelog);
	}

	@Test
	void mainFileShouldBeValid() throws CurseException {
		assertThat(file.mainFileID()).isGreaterThanOrEqualTo(CurseAPI.MIN_FILE_ID);
		assertThat(file.mainFile()).isNotNull().isEqualTo(file.refreshMainFile());
	}

	@BeforeAll
	static void getFile() throws CurseException {
		final Optional<CurseFile> optionalFile = CurseAPI.file(258205, 2758483);
		assertThat(optionalFile).isPresent();

		final CurseFile file = optionalFile.get();
		assertThat(file.hasAlternateFile()).isTrue();
		CurseAlternateFileTest.file = file.alternateFile();
	}
}
