package com.therandomlabs.curseapi.forgesvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseAPIProvider;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFiles;
import com.therandomlabs.curseapi.game.CurseCategory;
import com.therandomlabs.curseapi.game.CurseGame;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.CurseSearchQuery;
import com.therandomlabs.curseapi.util.RetrofitUtils;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Element;

/**
 * A {@link CurseAPIProvider} that uses the API at {@code https://addons-ecs.forgesvc.net/}
 * used by the Twitch launcher.
 * <p>
 * This provider falls back on the methods declared in
 * {@link com.therandomlabs.curseapi.CurseAPI} wherever possible so that default behaviors
 * may be overridden. For example, {@link CurseProject#files()} is implemented by
 * calling {@link com.therandomlabs.curseapi.CurseAPI#files(int)} rather than directly
 * calling {@link #files(int)}.
 * <p>
 * Where possible, this class should not be accessed directly, and the methods declared in
 * {@link com.therandomlabs.curseapi.CurseAPI} should be favored.
 */
public final class ForgeSVCProvider implements CurseAPIProvider {
	/**
	 * The singleton instance of {@link ForgeSVCProvider}.
	 */
	public static final ForgeSVCProvider instance = new ForgeSVCProvider();

	private static final ForgeSVC forgeSVC =
			RetrofitUtils.get("https://addons-ecs.forgesvc.net/").create(ForgeSVC.class);

	private ForgeSVCProvider() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CurseProject project(int id) throws CurseException {
		return RetrofitUtils.execute(forgeSVC.getProject(id));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element projectDescription(int id) throws CurseException {
		final Element element = RetrofitUtils.getElement(forgeSVC.getDescription(id));
		return element == null ? null : element.child(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CurseProject> searchProjects(CurseSearchQuery query) throws CurseException {
		final List<ForgeSVCProject> projects = RetrofitUtils.execute(forgeSVC.searchProjects(
				query.gameID(), query.categorySectionID(), query.categoryID(),
				query.gameVersionString(), query.pageIndex(), query.pageSize(),
				query.searchFilter(), query.sortingMethod().id()
		));

		if (projects == null) {
			throw new CurseException("Failed to search projects: " + query);
		}

		return new ArrayList<>(projects);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CurseFiles<CurseFile> files(int projectID) throws CurseException {
		final Set<ForgeSVCFile> files = RetrofitUtils.execute(forgeSVC.getFiles(projectID));

		if (files == null) {
			return null;
		}

		for (ForgeSVCFile file : files) {
			file.setProjectID(projectID);
		}

		return new CurseFiles<>(files);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CurseFile file(int projectID, int fileID) throws CurseException {
		final ForgeSVCFile file = RetrofitUtils.execute(forgeSVC.getFile(projectID, fileID));

		if (file == null) {
			return null;
		}

		file.setProjectID(projectID);
		return file;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param projectID a project ID. This is apparently not necessary, so {@code 0} will suffice.
	 */
	@Override
	public Element fileChangelog(int projectID, int fileID) throws CurseException {
		Element element = RetrofitUtils.getElement(forgeSVC.getChangelog(projectID, fileID));

		if (element == null) {
			return null;
		}

		element = element.child(0);
		return element == null ? CurseAPI.NO_CHANGELOG_PROVIDED : element;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param projectID a project ID. This is apparently not necessary, so {@code 0} will suffice.
	 */
	@Override
	public HttpUrl fileDownloadURL(int projectID, int fileID) throws CurseException {
		final String url = RetrofitUtils.getString(forgeSVC.getFileDownloadURL(projectID, fileID));
		return url == null ? null : HttpUrl.get(url);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<CurseGame> games() throws CurseException {
		final Set<ForgeSVCGame> games = RetrofitUtils.execute(forgeSVC.getGames(false));

		if (games == null) {
			throw new CurseException("Failed to retrieve games");
		}

		return new TreeSet<>(games);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CurseGame game(int id) throws CurseException {
		return RetrofitUtils.execute(forgeSVC.getGame(id));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<CurseCategory> categories() throws CurseException {
		final Set<ForgeSVCCategory> categories = RetrofitUtils.execute(forgeSVC.getCategories());

		if (categories == null) {
			throw new CurseException("Failed to retrieve categories");
		}

		return new TreeSet<>(categories);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<CurseCategory> categories(int sectionID) throws CurseException {
		final Set<ForgeSVCCategory> categories =
				RetrofitUtils.execute(forgeSVC.getCategories(sectionID));
		return categories == null ? null : new TreeSet<>(categories);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CurseCategory category(int id) throws CurseException {
		return RetrofitUtils.execute(forgeSVC.getCategory(id));
	}
}
