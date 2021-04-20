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

package com.therandomlabs.curseapi;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.therandomlabs.curseapi.cfwidget.CFWidgetProvider;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFiles;
import com.therandomlabs.curseapi.forgesvc.ForgeSvcProvider;
import com.therandomlabs.curseapi.game.CurseCategory;
import com.therandomlabs.curseapi.game.CurseGame;
import com.therandomlabs.curseapi.game.CurseGameVersion;
import com.therandomlabs.curseapi.game.CurseGameVersionGroup;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.CurseSearchQuery;
import com.therandomlabs.curseapi.util.CheckedFunction;
import com.therandomlabs.curseapi.util.JsoupUtils;
import com.therandomlabs.curseapi.util.OkHttpUtils;
import okhttp3.HttpUrl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main CurseAPI class.
 * <p>
 * Contains methods for retrieving {@link CurseProject} and {@link CurseFile} instances as well
 * as for managing {@link CurseAPIProvider}s.
 */
public final class CurseAPI {
	/**
	 * The minimum CurseForge game ID.
	 */
	public static final int MIN_GAME_ID = 1;

	/**
	 * The minimum CurseForge category section ID.
	 */
	public static final int MIN_CATEGORY_SECTION_ID = 1;

	/**
	 * The minimum CurseForge category ID.
	 */
	public static final int MIN_CATEGORY_ID = 1;

	/**
	 * The minimum CurseForge project ID.
	 */
	public static final int MIN_PROJECT_ID = 10;

	/**
	 * The minimum CurseForge file ID.
	 */
	public static final int MIN_FILE_ID = 60018;

	/**
	 * The minimum CurseForge project attachment ID.
	 */
	public static final int MIN_ATTACHMENT_ID = 76990;

	private static final Logger logger = LoggerFactory.getLogger(CurseAPI.class);

	private static final List<CurseAPIProvider> providers = Lists.newArrayList(
			ForgeSvcProvider.instance,
			CFWidgetProvider.instance
	);

	private CurseAPI() {}

	/**
	 * Returns a {@link CurseProject} instance for the specified project ID.
	 *
	 * @param id a project ID.
	 * @return a {@link CurseProject} instance for the specified project ID wrapped in an
	 * {@link Optional} if the project exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<CurseProject> project(int id) throws CurseException {
		CursePreconditions.checkProjectID(id, "id");
		return get(provider -> provider.project(id));
	}

	/**
	 * Returns a {@link CurseProject} instance for the project with the specified URL path.
	 *
	 * @param path a project URL path.
	 * @return a {@link CurseProject} instance for the project with the specified URL path
	 * wrapped in an {@link Optional} if it exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<CurseProject> project(String path) throws CurseException {
		Preconditions.checkNotNull(path, "path should not be null");
		Preconditions.checkArgument(!path.isEmpty(), "path should not be empty");
		return get(provider -> provider.project(path));
	}

	/**
	 * Returns a {@link CurseProject} instance for the project with the specified URL.
	 *
	 * @param url a project URL..
	 * @return a {@link CurseProject} instance for the project with the specified URL
	 * wrapped in an {@link Optional} if it exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<CurseProject> project(HttpUrl url) throws CurseException {
		Preconditions.checkNotNull(url, "url should not be null");
		Preconditions.checkArgument(
				"curseforge.com".equals(url.topPrivateDomain()),
				"url should be a CurseForge project URL"
		);
		return project(url.encodedPath());
	}

	/**
	 * Returns the description for the project with the specified ID.
	 *
	 * @param id a project ID.
	 * @return an {@link Element} containing the description for the project with the specified ID
	 * wrapped in an {@link Optional} if the project exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<Element> projectDescription(int id) throws CurseException {
		CursePreconditions.checkProjectID(id, "id");
		return get(provider -> provider.projectDescription(id));
	}

	/**
	 * Returns the description for the project with the specified ID as plain text.
	 *
	 * @param id a project ID.
	 * @return the description for the project with the specified ID as plain text wrapped in an
	 * {@link Optional} if the project exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 * @see JsoupUtils#getPlainText(Element, int)
	 */
	public static Optional<String> projectDescriptionPlainText(int id)
			throws CurseException {
		return projectDescriptionPlainText(id, Integer.MAX_VALUE);
	}

	/**
	 * Returns the description for the project with the specified ID as plain text.
	 *
	 * @param id a project ID.
	 * @param maxLineLength the maximum length of a line. This value is used for word wrapping.
	 * @return the description for the project with the specified ID as plain text wrapped in an
	 * {@link Optional} if the project exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 * @see JsoupUtils#getPlainText(Element, int)
	 */
	public static Optional<String> projectDescriptionPlainText(int id, int maxLineLength)
			throws CurseException {
		CursePreconditions.checkProjectID(id, "id");
		Preconditions.checkArgument(maxLineLength > 0, "maxLineLength should be greater than 0");
		return projectDescription(id).map(
				description -> JsoupUtils.getPlainText(description, maxLineLength).trim()
		);
	}

	/**
	 * Executes a {@link CurseSearchQuery}.
	 *
	 * @param query a {@link CurseSearchQuery}.
	 * @return a mutable {@link List} of {@link CurseProject}s that match the specified query
	 * wrapped in an {@link Optional} if the query is successful, or otherwise
	 * {@link Optional#empty()}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<List<CurseProject>> searchProjects(CurseSearchQuery query)
			throws CurseException {
		Preconditions.checkNotNull(query, "query should not be null");
		return get(provider -> provider.searchProjects(query));
	}

	/**
	 * Returns a {@link CurseFiles} instance for the specified project ID.
	 *
	 * @param projectID a project ID.
	 * @return a {@link CurseFiles} instance for the specified project ID wrapped in an
	 * {@link Optional} if the project exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<CurseFiles<CurseFile>> files(int projectID) throws CurseException {
		CursePreconditions.checkProjectID(projectID, "projectID");
		return get(provider -> provider.files(projectID));
	}

	/**
	 * Returns a {@link CurseFile} instance for the specified project and file ID.
	 * Note that if the specified file is an alternate file, a
	 * {@link com.therandomlabs.curseapi.file.BasicCurseFile.Immutable} instance should be
	 * created instead, as {@link CurseFile} instances cannot be retrieved for alternate files.
	 *
	 * @param projectID a project ID.
	 * @param fileID a file ID.
	 * @return a {@link CurseFile} instance for the specified project and file ID wrapped in an
	 * {@link Optional} if the file exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<CurseFile> file(int projectID, int fileID) throws CurseException {
		CursePreconditions.checkProjectID(projectID, "projectID");
		CursePreconditions.checkFileID(fileID, "fileID");
		return get(provider -> provider.file(projectID, fileID));
	}

	/**
	 * Returns the changelog for the specified project and file ID.
	 *
	 * @param projectID a project ID.
	 * @param fileID a file ID.
	 * @return an {@link Element} containing the changelog for the specified project and file ID
	 * wrapped in an {@link Optional} if the file exists, or otherwise an empty {@link Optional}.
	 * If no changelog is provided for the specified file, the {@link Element} is empty.
	 * @throws CurseException if an error occurs.
	 * @see JsoupUtils#emptyElement()
	 */
	public static Optional<Element> fileChangelog(int projectID, int fileID) throws CurseException {
		CursePreconditions.checkProjectID(projectID, "projectID");
		CursePreconditions.checkFileID(fileID, "fileID");
		return get(provider -> provider.fileChangelog(projectID, fileID));
	}

	/**
	 * Returns the changelog for the specified project and file ID as plain text.
	 *
	 * @param projectID a project ID.
	 * @param fileID a file ID.
	 * @return the changelog for the specified project and file ID as plain text if the file exists,
	 * or otherwise an empty {@link Optional}. If no changelog is provided for the specified file,
	 * the string wrapped in the {@link Optional} is empty.
	 * @throws CurseException if an error occurs.
	 * @see JsoupUtils#getPlainText(Element, int)
	 */
	public static Optional<String> fileChangelogPlainText(int projectID, int fileID)
			throws CurseException {
		return fileChangelogPlainText(projectID, fileID, Integer.MAX_VALUE);
	}

	/**
	 * Returns the changelog for the specified project and file ID as plain text.
	 *
	 * @param projectID a project ID.
	 * @param fileID a file ID.
	 * @param maxLineLength the maximum length of a line. This value is used for word wrapping.
	 * @return the changelog for the specified project and file ID as plain text if the file exists,
	 * or otherwise an empty {@link Optional}. If no changelog is provided for the specified file,
	 * the string wrapped in the {@link Optional} is empty.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<String> fileChangelogPlainText(
			int projectID, int fileID, int maxLineLength
	) throws CurseException {
		CursePreconditions.checkProjectID(projectID, "projectID");
		CursePreconditions.checkFileID(fileID, "fileID");
		Preconditions.checkArgument(maxLineLength > 0, "maxLineLength should be greater than 0");
		return fileChangelog(projectID, fileID).map(
				changelog -> JsoupUtils.getPlainText(changelog, maxLineLength).trim()
		);
	}

	/**
	 * Returns the download URL for the specified project and file ID.
	 *
	 * @param projectID a project ID.
	 * @param fileID a file ID.
	 * @return the download URL for the specified project and file ID wrapped in an
	 * {@link Optional} if the file exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<HttpUrl> fileDownloadURL(int projectID, int fileID)
			throws CurseException {
		CursePreconditions.checkProjectID(projectID, "projectID");
		CursePreconditions.checkFileID(fileID, "fileID");
		return get(provider -> provider.fileDownloadURL(projectID, fileID));
	}

	/**
	 * Downloads the file with the specified project and file ID to the specified {@link Path}.
	 *
	 * @param projectID a project ID.
	 * @param fileID a file ID.
	 * @param path a {@link Path}.
	 * @return {@code true} if the file downloads successfully, or otherwise {@code false}.
	 * @throws CurseException if an error occurs.
	 */
	public static boolean downloadFile(int projectID, int fileID, Path path) throws CurseException {
		CursePreconditions.checkProjectID(projectID, "projectID");
		CursePreconditions.checkFileID(fileID, "fileID");
		Preconditions.checkNotNull(path, "path should not be null");

		final Optional<HttpUrl> optionalURL = fileDownloadURL(projectID, fileID);

		if (!optionalURL.isPresent()) {
			return false;
		}

		OkHttpUtils.download(optionalURL.get(), path);
		return true;
	}

	/**
	 * Downloads the file with the specified project and file ID to the specified directory.
	 *
	 * @param projectID a project ID.
	 * @param fileID a file ID.
	 * @param directory a {@link Path} to a directory.
	 * @return a {@link Path} to the downloaded file wrapped in an {@link Optional} if the
	 * download is successful, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 * @see OkHttpUtils#getFileNameFromURLPath(HttpUrl)
	 */
	public static Optional<Path> downloadFileToDirectory(int projectID, int fileID, Path directory)
			throws CurseException {
		CursePreconditions.checkProjectID(projectID, "projectID");
		CursePreconditions.checkFileID(fileID, "fileID");
		Preconditions.checkNotNull(directory, "directory should not be null");

		final Optional<HttpUrl> optionalURL = fileDownloadURL(projectID, fileID);

		if (!optionalURL.isPresent()) {
			return Optional.empty();
		}

		final HttpUrl url = optionalURL.get();
		return Optional.of(OkHttpUtils.downloadToDirectory(
				url, directory, OkHttpUtils.getFileNameFromURLPath(url)
		));
	}

	/**
	 * Returns all games that CurseForge supports.
	 *
	 * @return a mutable {@link Set} containing {@link CurseGame} instances that represent
	 * all games supported by CurseForge wrapped in an {@link Optional} if it can be retrieved,
	 * or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<Set<CurseGame>> games() throws CurseException {
		return get(CurseAPIProvider::games);
	}

	/**
	 * Returns a {@link Stream} of all games that CurseForge supports.
	 *
	 * @return a {@link Stream} of all games that CurseForge supports,
	 * or {@link Stream#empty()} if they cannot be retrieved.
	 * @throws CurseException if an error occurs.
	 */
	public static Stream<CurseGame> streamGames() throws CurseException {
		final Optional<Set<CurseGame>> optionalGames = games();
		return optionalGames.map(Set::stream).orElseGet(Stream::empty);
	}

	/**
	 * Returns the CurseForge game with the specified ID.
	 *
	 * @param id a game ID.
	 * @return a {@link CurseGame} instance that represents the CurseForge game with the specified
	 * ID wrapped in an {@link Optional} if it exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<CurseGame> game(int id) throws CurseException {
		CursePreconditions.checkGameID(id, "id");
		return get(provider -> provider.game(id));
	}

	/**
	 * Returns all game versions of the game with the specified ID supported by CurseForge.
	 *
	 * @param gameID a game ID.
	 * @param <V> the implementation of {@link CurseGameVersion}.
	 * @return a mutable {@link NavigableSet} containing {@link CurseGameVersion} instances that
	 * represent all game versions of the game with the specified ID supported by CurseForge wrapped
	 * in an {@link Optional} if it can be retrieved, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	@SuppressWarnings("unchecked")
	public static <V extends CurseGameVersion<?>> Optional<NavigableSet<V>> gameVersions(int gameID)
			throws CurseException {
		CursePreconditions.checkGameID(gameID, "gameID");
		return get(provider -> (NavigableSet<V>) provider.gameVersions(gameID));
	}

	/**
	 * Returns the game version of the game with the specified ID with the specified version string.
	 *
	 * @param gameID a game ID.
	 * @param versionString a version string. The version string may be empty but should never
	 * be {@code null}.
	 * @param <V> the implementation of {@link CurseGameVersion}.
	 * @return a {@link CurseGameVersion} instance that represents the game version of the game
	 * with the specified ID with the specified version string wrapped in an {@link Optional}
	 * if it exists, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	@SuppressWarnings("unchecked")
	public static <V extends CurseGameVersion<?>> Optional<V> gameVersion(
			int gameID, String versionString
	) throws CurseException {
		CursePreconditions.checkGameID(gameID, "gameID");
		Preconditions.checkNotNull(versionString, "versionString should not be null");
		return get(provider -> (V) provider.gameVersion(gameID, versionString));
	}

	/**
	 * Returns all project categories on CurseForge.
	 *
	 * @return a mutable {@link Set} containing {@link CurseCategory} instances that represent
	 * all project categories on CurseForge wrapped in an {@link Optional} if it can be retrieved,
	 * or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<Set<CurseCategory>> categories() throws CurseException {
		return get(CurseAPIProvider::categories);
	}

	/**
	 * Returns all categories in a category section.
	 *
	 * @param sectionID a category section ID.
	 * @return a mutable {@link Set} containing {@link CurseCategory} instances that represent
	 * all categories in the category section with the specified ID wrapped in an optional if it
	 * can be retrieved, or otherwise an empty {@link Optional}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<Set<CurseCategory>> categories(int sectionID) throws CurseException {
		CursePreconditions.checkCategorySectionID(sectionID, "sectionID");
		return get(provider -> provider.categories(sectionID));
	}

	/**
	 * Returns a {@link Stream} of all CurseForge categories.
	 *
	 * @return a {@link Stream} of all CurseForge categories, or {@link Stream#empty()} if they
	 * cannot be retrieved.
	 * @throws CurseException if an error occurs.
	 */
	public static Stream<CurseCategory> streamCategories() throws CurseException {
		final Optional<Set<CurseCategory>> optionalCategories = categories();
		return optionalCategories.map(Set::stream).orElseGet(Stream::empty);
	}

	/**
	 * Returns a {@link Stream} of all CurseForge categories in a category section.
	 *
	 * @param sectionID a category section ID.
	 * @return a {@link Stream} of all CurseForge categories in a category section,
	 * or {@link Stream#empty()} if they cannot be retrieved.
	 * @throws CurseException if an error occurs.
	 */
	public static Stream<CurseCategory> streamCategories(int sectionID) throws CurseException {
		final Optional<Set<CurseCategory>> optionalCategories = categories(sectionID);
		return optionalCategories.map(Set::stream).orElseGet(Stream::empty);
	}

	/**
	 * Returns the CurseForge category with the specified ID.
	 *
	 * @param id a category ID.
	 * @return a {@link CurseCategory} instance that represents the CurseForge category with the
	 * specified ID wrapped in an {@link Optional} if it exists, or otherwise
	 * {@link Optional#empty()}.
	 * @throws CurseException if an error occurs.
	 */
	public static Optional<CurseCategory> category(int id) throws CurseException {
		CursePreconditions.checkCategoryID(id, "id");
		return get(provider -> provider.category(id));
	}

	/**
	 * Returns a {@link Set} of {@link CurseGameVersionGroup}s for the specified
	 * {@link CurseGameVersion}s.
	 *
	 * @param versions an array of {@link CurseGameVersion}s.
	 * @param <V> the type of {@link CurseGameVersion}.
	 * @return a mutable {@link Set} of {@link CurseGameVersionGroup}s for the specified
	 * {@link CurseGameVersion}s.
	 */
	@SuppressWarnings("varargs")
	@SafeVarargs
	public static <V extends CurseGameVersion<?>> Set<CurseGameVersionGroup<V>> gameVersionGroups(
			V... versions
	) {
		return gameVersionGroups(Arrays.asList(versions));
	}

	/**
	 * Returns a {@link Set} of {@link CurseGameVersionGroup}s for the specified
	 * {@link CurseGameVersion}s.
	 *
	 * @param versions a {@link Collection} of {@link CurseGameVersion}s.
	 * @param <V> the type of {@link CurseGameVersion}.
	 * @return a mutable {@link Set} of {@link CurseGameVersionGroup}s for the specified
	 * {@link CurseGameVersion}s.
	 */
	@SuppressWarnings("unchecked")
	public static <V extends CurseGameVersion<?>> Set<CurseGameVersionGroup<V>> gameVersionGroups(
			Collection<? extends V> versions
	) {
		return versions.stream().
				map(version -> (CurseGameVersionGroup<V>) version.versionGroup()).
				filter(group -> !group.isNone()).
				collect(Collectors.toCollection(HashSet::new));
	}

	/**
	 * Returns a collection derived from the elements of the specified {@link Collection} by
	 * applying the specified mapping function.
	 * <p>
	 * The advantage of this method over the traditional {@link java.util.stream.Stream}
	 * methods is that it uses {@link CheckedFunction}s rather than regular {@link Function}s,
	 * and this allows methods that throw {@link CurseException}s such as
	 * {@link CurseFile#changelog()} to be called.
	 * <p>
	 * {@link Collection#parallelStream()} is used to iterate over elements of the
	 * {@link Collection}, meaning that time-consuming requests may be executed in parallel.
	 *
	 * @param collection a {@link Collection} to derive the result from.
	 * @param function a {@link CheckedFunction} that maps objects of type {@code E} to objects of
	 * type {@code R}.
	 * @param collector a {@link Collector}.
	 * @param <E> the type of the elements.
	 * @param <R> the type of the resultant values.
	 * @param <C> the type of the resultant collection.
	 * @return a collection derived from the elements of the specified {@link Collection}.
	 * @throws CurseException if an error occurs.
	 */
	public static <E, R, C> C parallelMap(
			Collection<? extends E> collection,
			CheckedFunction<? super E, ? extends R, CurseException> function,
			Collector<? super R, ?, C> collector
	) throws CurseException {
		try {
			return collection.parallelStream().
					map(element -> callCheckedFunction(element, function)).
					collect(collector);
		} catch (RuntimeException ex) {
			if (ex.getCause() instanceof CurseException) {
				throw (CurseException) ex.getCause();
			}

			throw ex;
		}
	}

	/**
	 * Returns a {@link Map} derived from the elements of the specified {@link Collection}.
	 * <p>
	 * The advantage of this method over the traditional {@link java.util.stream.Stream}
	 * methods is that it uses {@link CheckedFunction}s rather than regular {@link Function}s,
	 * and this allows methods that throw {@link CurseException}s such as
	 * {@link CurseFile#changelog()} to be called.
	 * <p>
	 * The key function and the value function are both called on each element to retrieve the keys
	 * and values of the {@link Map} respectively. Additionally, {@link Collection#parallelStream()}
	 * is used to iterate over elements of the {@link Collection}, meaning that time-consuming
	 * requests may be executed in parallel.
	 *
	 * @param collection a {@link Collection} to derive the result from.
	 * @param keyMapper a {@link CheckedFunction} that maps objects of type {@code E} to objects of
	 * type {@code K}.
	 * @param valueMapper a {@link CheckedFunction} that maps objects of type {@code E} to objects
	 * of type {@code V}.
	 * @param <E> the type of the elements.
	 * @param <K> the type of the keys.
	 * @param <V> the type of the values.
	 * @return a {@link Map} derived from the elements of the specified {@link Collection}.
	 * @throws CurseException if an error occurs.
	 */
	public static <E, K, V> Map<Object, Object> parallelMap(
			Collection<? extends E> collection,
			CheckedFunction<? super E, ? extends K, CurseException> keyMapper,
			CheckedFunction<? super E, ? extends V, CurseException> valueMapper
	) throws CurseException {
		return parallelMap(
				collection,
				element -> new AbstractMap.SimpleEntry<>(
						keyMapper.apply(element),
						valueMapper.apply(element)
				),
				Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
		);
	}

	/**
	 * Registers a {@link CurseAPIProvider} if has not already been registered.
	 *
	 * @param provider a {@link CurseAPIProvider} instance.
	 * @param firstPriority {@code true} if the {@link CurseAPIProvider} should be put before
	 * all currently registered {@link CurseAPIProvider}s, or otherwise {@code false}.
	 * @return {@code true} if the {@link CurseAPIProvider} was registered,
	 * or otherwise {@code false}.
	 */
	public static boolean addProvider(CurseAPIProvider provider, boolean firstPriority) {
		Preconditions.checkNotNull(provider, "provider should not be null");

		if (providers.contains(provider)) {
			return false;
		}

		if (firstPriority) {
			providers.add(0, provider);
		} else {
			providers.add(provider);
		}

		return true;
	}

	/**
	 * Unregisters a {@link CurseAPIProvider}.
	 *
	 * @param provider a {@link CurseAPIProvider} instance.
	 * @return {@code true} if the {@link CurseAPIProvider} was registered,
	 * or otherwise {@code false}.
	 */
	public static boolean removeProvider(CurseAPIProvider provider) {
		Preconditions.checkNotNull(provider, "provider should not be null");
		return providers.remove(provider);
	}

	/**
	 * Returns a mutable {@link List} of all registered {@link CurseAPIProvider}s.
	 *
	 * @return a mutable {@link List} of all registered {@link CurseAPIProvider}s.
	 */
	public static List<CurseAPIProvider> providers() {
		return new ArrayList<>(providers);
	}

	private static <T> Optional<T> get(
			CheckedFunction<CurseAPIProvider, T, CurseException> function
	) throws CurseException {
		if (providers.isEmpty()) {
			logger.warn("No CurseAPIProviders configured");
			return Optional.empty();
		}

		for (CurseAPIProvider provider : providers) {
			final T t = function.apply(provider);

			if (t != null) {
				return Optional.of(t);
			}
		}

		return Optional.empty();
	}

	@Nullable
	private static <E, T> T callCheckedFunction(
			E element, CheckedFunction<E, T, CurseException> function
	) {
		try {
			return function.apply(element);
		} catch (CurseException ex) {
			throw new RuntimeException(ex);
		}
	}
}
