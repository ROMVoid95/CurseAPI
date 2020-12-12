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

package com.therandomlabs.curseapi.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import com.google.common.collect.Iterables;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFiles;
import com.therandomlabs.curseapi.game.CurseCategory;
import com.therandomlabs.curseapi.game.CurseCategorySection;
import com.therandomlabs.curseapi.game.CurseGame;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CurseProjectTest {
	private static CurseProject project;
	private static CurseProject comparisonProject;

	@Test
	void comparatorsShouldBeValid() {
		assertThat(CurseProject.SORT_BY_OLDEST.compare(project, comparisonProject)).
				isGreaterThan(0);
		assertThat(CurseProject.SORT_BY_NEWEST.compare(project, comparisonProject)).isLessThan(0);
	}

	@Test
	void equalsShouldBeValid() {
		assertThat(project).
				isEqualTo(project).
				isNotEqualTo(comparisonProject).
				isNotEqualTo(null);
	}

	@Test
	void toStringShouldNotBeEmpty() {
		assertThat(project.toString()).isNotEmpty();
	}

	@Test
	void compareToShouldBeBasedOnName() {
		assertThat(project).isEqualByComparingTo(project);
		assertThat(project.compareTo(comparisonProject)).isNegative();
	}

	@Test
	void idShouldBeValid() {
		assertThat(project.id()).isGreaterThanOrEqualTo(CurseAPI.MIN_PROJECT_ID);
	}

	@Test
	void nameShouldNotBeEmpty() {
		assertThat(project.name()).isNotEmpty();
	}

	@Test
	void authorShouldNotBeNull() {
		assertThat(project.author()).isNotNull();
	}

	@Test
	void authorsShouldContainAuthor() {
		assertThat(project.authors()).contains(project.author());
	}

	@Test
	void exceptionShouldBeThrownIfAttachmentIDIsInvalid() {
		assertThatThrownBy(() -> project.attachment(CurseAPI.MIN_ATTACHMENT_ID - 1)).
				isInstanceOf(IllegalArgumentException.class).
				hasMessageContaining("should not be smaller than");
	}

	@Test
	void attachmentsShouldBeValid() throws CurseException {
		assertThat(project.attachments()).isNotEmpty();

		final CurseAttachment attachment = Iterables.getFirst(project.attachments(), null);

		assertThat(attachment).
				isNotNull().
				isNotEqualTo(null);
		assertThat(attachment.toString()).isNotEmpty();
		assertThat(attachment.id()).isGreaterThanOrEqualTo(CurseAPI.MIN_ATTACHMENT_ID);
		assertThat(project.attachment(attachment.id())).isEqualTo(attachment);
		assertThat(attachment.title()).isNotEmpty();
		assertThat(attachment.descriptionPlainText()).isNotNull();
		assertThat(attachment.url()).isNotNull();
		assertThat(attachment.get()).isNotNull();
		assertThat(attachment.thumbnailURL()).isNotNull();
		assertThat(attachment.thumbnail()).isNotNull();
	}

	@Test
	void logoShouldNotBeNullOrPlaceholder() {
		assertThat(project.logo()).
				isNotNull().
				isNotEqualTo(CurseAttachment.PLACEHOLDER_LOGO);
	}

	@Test
	void urlShouldNotBeNull() {
		assertThat(project.url()).isNotNull();
	}

	@Test
	void gameIDShouldBeValid() {
		assertThat(project.gameID()).isGreaterThanOrEqualTo(CurseAPI.MIN_GAME_ID);
	}

	@Test
	void gameShouldBeValid() throws CurseException {
		final CurseGame game1 = project.game();
		assertThat(game1).isNotNull();

		final CurseGame game2 = project.refreshGame();
		assertThat(game1.id()).isEqualTo(project.gameID());

		assertThat(game1).isEqualTo(game2);
	}

	@Test
	void summaryShouldNotBeEmpty() {
		assertThat(project.summary()).isNotEmpty();
	}

	@Test
	void exceptionShouldBeThrownIfMaxLineLengthIsInvalid() throws CurseException {
		assertThatThrownBy(() -> project.descriptionPlainText(0)).
				isInstanceOf(IllegalArgumentException.class).
				hasMessageContaining("should be greater than 0");
	}

	@Test
	void descriptionPlainTextShouldBeValid() throws CurseException {
		final String description = project.descriptionPlainText();
		assertThat(description).isNotEmpty();

		project.refreshDescription();
		assertThat(project.descriptionPlainText()).isEqualTo(description);
	}

	@Test
	void downloadCountShouldBePositive() throws CurseException {
		assertThat(project.downloadCount()).isPositive();
	}

	@Test
	void filesShouldBeValid() throws CurseException {
		final CurseFiles<CurseFile> files = project.files();
		assertThat(files).isNotEmpty();

		for (CurseFile file : files) {
			assertThat(file.project()).isEqualTo(project);
		}

		assertThat(files).isEqualTo(project.refreshFiles());
	}

	@Test
	void exceptionShouldBeThrownIfFileIDIsInvalid() {
		assertThatThrownBy(() -> project.fileURL(CurseAPI.MIN_FILE_ID - 1)).
				isInstanceOf(IllegalArgumentException.class).
				hasMessageContaining("should not be smaller than");
	}

	@Test
	void fileURLShouldBeValid() throws CurseException {
		assertThat(project.fileURL(CurseAPI.MIN_FILE_ID)).isNotNull();
	}

	@Test
	void primaryCategoryShouldBeValid() {
		assertThat(project.primaryCategory()).isNotNull();
		assertThat(project.primaryCategory().sectionID()).
				isGreaterThanOrEqualTo(CurseAPI.MIN_CATEGORY_SECTION_ID);
		assertThat(project.primaryCategory().slug()).isNotNull();
	}

	@Test
	void categoriesShouldContainPrimaryCategory() {
		assertThat(project.categories()).contains(project.primaryCategory());
	}

	@Test
	void categorySectionShouldBeValid() throws CurseException {
		final CurseCategorySection categorySection = project.categorySection();
		assertThat(categorySection).isNotNull();

		//We also test CurseCategorySection here.
		final Optional<CurseCategory> optionalCategory = CurseAPI.category(400);
		assertThat(optionalCategory).isPresent();
		final CurseCategory category = optionalCategory.get();

		final Optional<CurseCategorySection> optionalCategorySection2 = category.section();
		assertThat(optionalCategorySection2).isPresent();

		assertThat(categorySection).
				isNotEqualTo(null).
				isNotEqualTo(optionalCategorySection2.get()).
				isEqualTo(categorySection);
		assertThat(categorySection.toString()).isNotEmpty();
		assertThat(categorySection.game()).isNotNull().isEqualTo(categorySection.refreshGame());
		assertThat(categorySection.categories()).
				isNotNull().
				isEqualTo(categorySection.refreshCategories());
		assertThat(categorySection.asCategory()).
				isNotNull().
				isEqualTo(categorySection.refreshAsCategory());
	}

	@Test
	void slugShouldNotBeNull() {
		assertThat(project.slug()).isNotNull();
	}

	@Test
	void creationTimeShouldNotBeNull() {
		assertThat(project.creationTime()).isNotNull();
	}

	@Test
	void lastUpdateTimeShouldNotBeNull() {
		assertThat(project.lastUpdateTime()).isNotNull();
	}

	@Test
	void lastModificationTimeShouldNotBeNull() {
		assertThat(project.lastModificationTime()).isNotNull();
	}

	@Test
	void experimentalShouldBeFalse() {
		assertThat(project.experimental()).isFalse();
	}

	@BeforeAll
	static void getProject() throws CurseException {
		final Optional<CurseProject> optionalProject = CurseAPI.project(285612);
		assertThat(optionalProject).isPresent();
		project = optionalProject.get();

		final Optional<CurseProject> optionalComparisonProject = CurseAPI.project(258205);
		assertThat(optionalComparisonProject).isPresent();
		comparisonProject = optionalComparisonProject.get();
	}
}
