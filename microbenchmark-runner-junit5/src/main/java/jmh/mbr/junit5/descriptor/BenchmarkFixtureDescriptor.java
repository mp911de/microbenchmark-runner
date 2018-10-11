/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jmh.mbr.junit5.descriptor;

import jmh.mbr.core.model.BenchmarkFixture;
import jmh.mbr.core.model.BenchmarkMethod;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * {@link org.junit.platform.engine.TestDescriptor} for a {@link BenchmarkFixture}.
 * 
 * @author Mark Paluch
 */
public class BenchmarkFixtureDescriptor extends AbstractTestDescriptor {

	private final BenchmarkFixture fixture;

	public BenchmarkFixtureDescriptor(UniqueId uniqueId, BenchmarkMethod method, BenchmarkFixture fixture) {
		super(uniqueId, fixture.getDisplayName(), MethodSource.from(method.getMethod()));
		this.fixture = fixture;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.junit.platform.engine.TestDescriptor#getType()
	 */
	@Override
	public Type getType() {
		return Type.TEST;
	}

	public BenchmarkFixture getFixture() {
		return fixture;
	}
}
