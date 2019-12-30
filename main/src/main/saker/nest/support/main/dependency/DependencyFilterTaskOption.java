package saker.nest.support.main.dependency;

import saker.nest.support.api.dependency.filter.DependencyFilter;

public class DependencyFilterTaskOption {
	private DependencyFilter filter;

	private DependencyFilterTaskOption(DependencyFilter filter) {
		this.filter = filter;
	}

	public DependencyFilter getFilter() {
		return filter;
	}

	public static DependencyFilterTaskOption valueOf(DependencyFilter filter) {
		return new DependencyFilterTaskOption(filter);
	}
}
