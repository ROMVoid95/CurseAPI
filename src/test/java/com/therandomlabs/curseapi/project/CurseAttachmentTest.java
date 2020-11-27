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

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import org.junit.jupiter.api.Test;

class CurseAttachmentTest {
	@Test
	void placeholderLogoShouldBeValid() throws CurseException {
		assertThat(CurseAttachment.PLACEHOLDER_LOGO.toString()).isNotEmpty();
		assertThat(CurseAttachment.PLACEHOLDER_LOGO.id()).
				isGreaterThanOrEqualTo(CurseAPI.MIN_ATTACHMENT_ID);
		assertThat(CurseAttachment.PLACEHOLDER_LOGO.title()).isNotEmpty();
		assertThat(CurseAttachment.PLACEHOLDER_LOGO.descriptionPlainText()).isNotEmpty();
		assertThat(CurseAttachment.PLACEHOLDER_LOGO.url()).isNotNull();
		assertThat(CurseAttachment.PLACEHOLDER_LOGO.get()).isNotNull();
		assertThat(CurseAttachment.PLACEHOLDER_LOGO.thumbnailURL()).isNotNull();
		assertThat(CurseAttachment.PLACEHOLDER_LOGO.thumbnail()).isNotNull();
	}
}
