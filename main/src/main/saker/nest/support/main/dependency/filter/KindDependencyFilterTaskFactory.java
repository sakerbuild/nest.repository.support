package saker.nest.support.main.dependency.filter;

import java.util.Collection;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.dependency.filter.DependencyFilter;
import saker.nest.support.impl.dependency.filter.KindDependencyFilter;
import saker.nest.support.main.TaskDocs.DocDependencyFilter;
import saker.nest.support.main.dependency.ResolveBundleDependencyTaskFactory;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocDependencyFilter.class))
@NestInformation("Gets a dependency filter that limits the dependencies to the specified kinds.\n"
		+ "The task creates a dependency filter that filters out any dependencies that aren't declared with "
		+ "any of the configured dependency kinds.\n" + "The task result can be used with the "
		+ ResolveBundleDependencyTaskFactory.TASK_NAME + "() task.")
@NestParameterInformation(value = "Kinds",
		aliases = { "", "Kind" },
		required = true,
		type = @NestTypeUsage(value = Collection.class, elementTypes = String.class),
		info = @NestInformation("Specifies the list of dependency kinds that should be used to filter out dependencies with other kinds.\n"
				+ "Any dependency that is not specified with any of the kind that is specified as an input will be not part of "
				+ "the dependency resolution.\n"
				+ "Specifying no kinds (i.e. empty list) will result in all dependencies being filtered out. It can be "
				+ "useful if one wants to only resolve the versions of a bundle."))
public class KindDependencyFilterTaskFactory extends FrontendTaskFactory<DependencyFilter> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "nest.dependency.filter.kind";

	@Override
	public ParameterizableTask<? extends DependencyFilter> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<DependencyFilter>() {

			@SakerInput(value = { "", "Kind", "Kinds" }, required = true)
			public Collection<String> kindsOption;

			@Override
			public DependencyFilter run(TaskContext taskcontext) throws Exception {
				return new KindDependencyFilter(ImmutableUtils.makeImmutableNavigableSet(kindsOption));
			}
		};
	}

}
