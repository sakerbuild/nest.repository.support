/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.nest.support.main;

import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.main.dependency.ResolveBundleDependencyTaskFactory;
import saker.nest.support.main.download.DownloadBundleTaskFactory;
import saker.nest.support.main.localize.LocalizeBundleTaskFactory;

public class TaskDocs {
	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "saker.nest.bundle.BundleIdentifier")
	@NestInformation("Nest repository bundle identifier.\n"
			+ "A bundle identifier consists of a bundle name and any amount of qualifiers. Bundle identifier have "
			+ "the format of <name>[-qualifier]*.\n"
			+ "The version qualifier in the format of v1.2.3 is a special meta-qualifier that may be interpreted in various "
			+ "way in a given context.")
	@NestFieldInformation(value = "VersionQualifier",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The version qualifier of the bundle identifier.\n"
					+ "The version qualifier is in the v1.2.3 format. May be null."))
	@NestFieldInformation(value = "VersionNumber",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The version number of the bundle identifier.\n"
					+ "The version number is in the 1.2.3 format. It doesn't have a preceeding 'v' character. May be null."))
	@NestFieldInformation(value = "Name",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The name part of the bundle identifier."))
	public static class DocBundleIdentifier {
	}

	@NestTypeInformation(kind = TypeInformationKind.PATH, qualifiedName = "saker.build.file.path.SakerPath")
	@NestInformation("A path object representing a file location with a root and list of name components.\n"
			+ "A path may be relative or absolute. Absolute paths contain a root, which may be either the slash / root or "
			+ "have the [a-z]+: format.\n"
			+ "Relative paths contain no root and consist only of name components. Only relative paths may contain the \"..\" "
			+ "name component and only at the start of theirs.\n"
			+ "All paths case sensitive and are handled in normalized format.")
	@NestFieldInformation(value = "Root",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The root component of the path.\n" + "May be null if the path is relative. \n"
					+ "Can be either the slash root: /\n" + "Or in the format [a-z]+:"))
	@NestFieldInformation(value = "FileName",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The file name component of the path.\n"
					+ "It is usually the last component of the path, but may be null in cases where the path doesn't have one.\n"
					+ "E.g. \"..\", or \"/\" doesn't have file names."))
	@NestFieldInformation(value = "LastName",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The last name component of the path.\n"
					+ "It is the last name part of the path. May be null if the path doesn't have any names "
					+ "when it is the empty path, or simply a root path."))
	@NestFieldInformation(value = "Parent",
			type = @NestTypeUsage(DocSakerPath.class),
			info = @NestInformation("The parent of the path.\n"
					+ "May be null if no parent exists for the path, meaning that this is an absolute path that has no names."))
	public static class DocSakerPath {
	}

	@NestTypeInformation(kind = TypeInformationKind.WILDCARD_PATH, qualifiedName = "saker.build.file.path.WildcardPath")
	@NestInformation("Wildcard paths represent a pattern that are used to match paths in order to determine their inclusion.\n"
			+ "Wildcard paths contain the * character that can match any number of arbitrary characters in a name component of a path.\n"
			+ "The special \"**\" name part can be used to match any number of name components for a given path.\n"
			+ "Wildcards which don't contain any * characters can be also used as wildcards, but they will only match a single path.")
	public static class DocWildcardPath {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.bundle.BundleKey")
	@NestInformation("An unique identifier object for a saker.nest bundle for the current storage configuration.\n"
			+ "The object uniquely identifies a bundle that is available in the current repository storage configuration. "
			+ "It uniquely identifies a bundle even if a bundle identifier is present multiple times in the configuration.\n"
			+ "This object can be passed to tasks that accept bundle keys as inputs.")
	@NestFieldInformation(value = "BundleIdentifier",
			type = @NestTypeUsage(DocBundleIdentifier.class),
			info = @NestInformation("The bundle identifier part of the bundle key."))
	public static class DocBundleKey {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.dependency.DependencyResolutionTaskOutput")
	@NestInformation("Output of Nest repository bundle dependency resolution.\n"
			+ "Generally, the output of the task is passed to other tasks to take the appropriate action with "
			+ "the resolution result. (E.g. classpath, downloading, etc...)")
	@NestFieldInformation(value = "Bundles",
			type = @NestTypeUsage(value = Collection.class, elementTypes = DocBundleKey.class),
			info = @NestInformation("Collection of bundle keys that are the result of the dependency resolution.\n"
					+ "Each bundle key uniquely identifies a bundle for the current storage configuration."))
	public static class DocDependencyResolutionTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.download.DownloadBundleTaskOutput")
	@NestInformation("Output of the " + DownloadBundleTaskFactory.TASK_NAME + "() bundle downloading task.\n"
			+ "The result provides access to the execution paths of the bundles and to each individually downloaded bundle.")
	@NestFieldInformation(value = "BundlePaths",
			type = @NestTypeUsage(value = Collection.class, elementTypes = { SakerPath.class }),
			info = @NestInformation("The list of execution paths that are the result of the bundle download.\n"
					+ "Each path in the collection is the result of a downloaded bundle."))
	@NestFieldInformation(value = "DownloadResults",
			type = @NestTypeUsage(value = Collection.class, elementTypes = { DocDownloadWorkerTaskOutput.class }),
			info = @NestInformation("The list of download results for each downloaded bundle.\n"
					+ "Unlike BundlePaths, DownloadResults provide access to other information apart from the download "
					+ "path, like BundleIdentifier."))
	public static class DocDownloadBundleTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.download.DownloadWorkerTaskOutput")
	@NestInformation("Output of a single bundle download operation.\n"
			+ "Provides access to the downloaded bundle execution Path, and the BundleIdentifier of the bundle.")
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The execution path of the download result.\n"
					+ "The file at the given path is the result of the bundle download."))
	@NestFieldInformation(value = "BundleIdentifier",
			type = @NestTypeUsage(DocBundleIdentifier.class),
			info = @NestInformation("The bundle identifier of the downloaded bundle."))
	public static class DocDownloadWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.localize.LocalizeBundleTaskOutput")
	@NestInformation("Output of the " + LocalizeBundleTaskFactory.TASK_NAME + "() bundle localizing task.\n"
			+ "The result provides access to the local file system paths of the bundles and to each individually localized bundle.")
	@NestFieldInformation(value = "BundleLocalPaths",
			type = @NestTypeUsage(value = Collection.class, elementTypes = { SakerPath.class }),
			info = @NestInformation("The list of local file system paths that are the result of the bundle localization.\n"
					+ "Each path in the collection is the result of a localized bundle."))
	@NestFieldInformation(value = "LocalizeResults",
			type = @NestTypeUsage(value = Collection.class, elementTypes = { DocLocalizeWorkerTaskOutput.class }),
			info = @NestInformation("The list of localization results for each localized bundle.\n"
					+ "Unlike BundlePaths, LocalizeResults provide access to other information apart from the download "
					+ "path, like BundleIdentifier."))
	public static class DocLocalizeBundleTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.localize.LocalizeWorkerTaskOutput")
	@NestFieldInformation(value = "LocalPath",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The local file system path of the localization result.\n"
					+ "The file at the given path is the result of the bundle localization."))
	@NestFieldInformation(value = "BundleIdentifier",
			type = @NestTypeUsage(DocBundleIdentifier.class),
			info = @NestInformation("The bundle identifier of the localized bundle."))
	@NestInformation("Output of a bundle localizing worker task.")
	public static class DocLocalizeWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.dependency.filter.DependencyFilter")
	@NestInformation("Represents a dependency filter that can be applied to a dependency resolution algorithm.\n"
			+ "A dependency filter can be used to limit the dependencies that should be part of the dependency resolution "
			+ "algorithm. This can be in order to filter out dependencies that shouldn't be used for a given use-case.\n"
			+ "Dependency filters are primarily used with the " + ResolveBundleDependencyTaskFactory.TASK_NAME
			+ "() task.\n"
			+ "The dependency filters is a programmatic API for the nest.repository.support bundle. Other Nest packages "
			+ "may implement their own dependency filters for their use-case.")
	public static class DocDependencyFilter {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.local.install.LocalInstallTaskOutput")
	@NestFieldInformation(value = "InstallResults",
			type = @NestTypeUsage(value = Collection.class, elementTypes = { DocInstallWorkerTaskOutput.class }),
			info = @NestInformation("The collection of task results for each installed bundle."))
	@NestInformation("Output of the local bundle installation task.\n"
			+ "Provides access to each installed bundle result.")
	public static class DocLocalInstallTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.local.install.LocalInstallWorkerTaskOutput")
	@NestFieldInformation(value = "BundleIdentifier",
			type = @NestTypeUsage(DocBundleIdentifier.class),
			info = @NestInformation("The bundle identifier of the installed bundle."))
	@NestFieldInformation(value = "BundleHash",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The hash of the installed bundle in hexa encoded format.\n"
					+ "The hash algorithm is the same as the repository uses for internal representation. (Implementation dependent.)"))
	@NestInformation("Output of a bundle installing worker task.")
	public static class DocInstallWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.server.upload.BundleUploadTaskOutput")
	@NestFieldInformation(value = "UploadResults",
			type = @NestTypeUsage(value = Collection.class, elementTypes = { DocBundleUploadWorkerTaskOutput.class }),
			info = @NestInformation("The collection of task results for each uploaded bundle."))
	@NestInformation("Output of the server bundle upload task.\n" + "Provides access to each uploaded bundle result.")
	public static class DocBundleUploadTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.nest.support.api.server.upload.BundleUploadWorkerTaskOutput")
	@NestFieldInformation(value = "BundleIdentifier",
			type = @NestTypeUsage(DocBundleIdentifier.class),
			info = @NestInformation("The bundle identifier of the uploaded bundle."))
	@NestFieldInformation(value = "MD5",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The MD5 hash of the uploaded bundle in hexa encoded format."))
	@NestFieldInformation(value = "SHA256",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The SHA256 hash of the uploaded bundle in hexa encoded format."))
	@NestInformation("Output of a bundle upload worker task.")
	public static class DocBundleUploadWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.build.file.path.SakerPath")
	@NestInformation("Execution path to the archive with additional saker.nest test instrumentation.")
	public static class DocInstrumentedArchivePath {
	}

	@NestTypeInformation(qualifiedName = "java.lang.String")
	@NestInformation("A version number.")
	public static class DocVersionNumber {

	}
}
