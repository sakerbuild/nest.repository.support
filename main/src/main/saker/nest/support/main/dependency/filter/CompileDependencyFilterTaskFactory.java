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
package saker.nest.support.main.dependency.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.nest.bundle.BundleInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.dependency.filter.DependencyFilter;
import saker.nest.support.impl.dependency.filter.CompileDependencyFilter;
import saker.nest.support.main.TaskDocs.DocDependencyFilter;
import saker.nest.support.main.dependency.ResolveBundleDependencyTaskFactory;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocDependencyFilter.class))
@NestInformation("Gets a dependency filter that limits the dependencies to the specified kinds and " + "based on the "
		+ CompileDependencyFilter.DEPENDENCY_META_COMPILE_TRANSITIVE + " meta-data.\n"
		+ "The task creates a dependency filter that filters out dependencies that aren't declared with "
		+ "any of the configured dependency kinds.\n"
		+ "If the CompileTransitive parameter is set to false, the task will also filter out the dependencies that are "
		+ "declared with the " + CompileDependencyFilter.DEPENDENCY_META_COMPILE_TRANSITIVE + ": false meta-data.\n"
		+ "The filter will also remove the private dependencies.\n " + "The task result can be used with the "
		+ ResolveBundleDependencyTaskFactory.TASK_NAME + "() task.\n"
		+ "Note that if the CompileTransitive parameter is not set to false, the dependency filter works the same way "
		+ "as if the " + KindDependencyFilterTaskFactory.TASK_NAME + "() task was used.")
@NestParameterInformation(value = "Kinds",
		aliases = { "", "Kind" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = String.class),
		info = @NestInformation("Specifies the list of dependency kinds that should be used to filter out dependencies with other kinds.\n"
				+ "Any dependency that is not specified with any of the kind that is specified as an input will be not part of "
				+ "the dependency resolution.\n"
				+ "Specifying no kinds (i.e. empty list) will result in all dependencies being filtered out.\n"
				+ "If not specified, the default dependency kind is classpath."))
@NestParameterInformation(value = "CompileTransitive",
		type = @NestTypeUsage(boolean.class),
		info = @NestInformation("Specifies whether or not " + CompileDependencyFilter.DEPENDENCY_META_COMPILE_TRANSITIVE
				+ " dependencies should be included.\n" + "The default is true.\n"
				+ "If this parameter is set to false, then any transitive dependency that is declared with the "
				+ CompileDependencyFilter.DEPENDENCY_META_COMPILE_TRANSITIVE
				+ ": false meta-data will be filtered out."))
public class CompileDependencyFilterTaskFactory extends FrontendTaskFactory<DependencyFilter> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "nest.dependency.filter.compile";

	@Override
	public ParameterizableTask<? extends DependencyFilter> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<DependencyFilter>() {

			@SakerInput(value = { "", "Kind", "Kinds" })
			public Collection<String> kindsOption = Collections.singleton(BundleInformation.DEPENDENCY_KIND_CLASSPATH);

			@SakerInput("CompileTransitive")
			public boolean compileTransitiveOption = true;

			@Override
			public DependencyFilter run(TaskContext taskcontext) throws Exception {
				NavigableSet<String> kindsset = ImmutableUtils.makeImmutableNavigableSet(kindsOption);
				DependencyFilter result;
				if (compileTransitiveOption) {
					result = CompileDependencyFilter.createTransitive(kindsset);
				} else {
					result = CompileDependencyFilter.createNonTransitive(kindsset);
				}
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
