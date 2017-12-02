package com.therandomlabs.curseapi.widget;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import com.therandomlabs.curseapi.ReleaseType;
import com.therandomlabs.curseapi.util.MiscUtils;

public final class DownloadInfo implements Cloneable {
	public int id;
	public URL url;
	public String name;
	public ReleaseType type;
	public String version;
	public String filesize;
	public String[] versions;
	public int downloads;
	public DateInfo uploaded_at;

	@Override
	public DownloadInfo clone() {
		final DownloadInfo info = new DownloadInfo();

		info.id = id;
		info.url = url;
		info.name = name;
		info.type = type;
		info.version = version;
		info.filesize = filesize;
		info.versions = versions.clone();
		info.uploaded_at = uploaded_at.clone();

		return info;
	}

	public static DownloadInfo fromFileInfo(FileInfo fileInfo) {
		final DownloadInfo info = new DownloadInfo();

		info.id = fileInfo.id;
		info.url = fileInfo.url;
		info.name = fileInfo.name;
		info.type = fileInfo.type;
		info.version = fileInfo.version;
		info.filesize = fileInfo.filesize;
		info.versions = fileInfo.versions.clone();

		info.uploaded_at = new DateInfo();
		info.uploaded_at.date = MiscUtils.parseTime(fileInfo.uploaded_at).
				format(DateTimeFormatter.ISO_INSTANT);
		info.uploaded_at.timezone_type = 1;
		info.uploaded_at.timezone = "+00:00";

		return info;
	}
}
