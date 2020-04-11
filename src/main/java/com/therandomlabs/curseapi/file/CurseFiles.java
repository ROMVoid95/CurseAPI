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

package com.therandomlabs.curseapi.file;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.CursePreconditions;
import com.therandomlabs.curseapi.util.CheckedFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An implementation of {@link TreeSet} with additional utility methods for working with
 * {@link CurseFile}s.
 *
 * @param <F> the type of {@link BasicCurseFile}.
 */
public class CurseFiles<F extends BasicCurseFile> extends TreeSet<F> {
	/**
	 * When used as a {@link Comparator} for a collection of {@link CurseFile}s,
	 * the {@link CurseFile}s are ordered from newest to oldest.
	 *
	 * @see #withComparator(Comparator)
	 */
	public static final Comparator<BasicCurseFile> SORT_BY_NEWEST = BasicCurseFile::compareTo;

	/**
	 * When used as a {@link Comparator} for a collection of {@link CurseFile}s,
	 * the {@link CurseFile}s are ordered from oldest to newest.
	 *
	 * @see #withComparator(Comparator)
	 */
	public static final Comparator<BasicCurseFile> SORT_BY_OLDEST = SORT_BY_NEWEST.reversed();

	private static final long serialVersionUID = 4762475826152943776L;

	/**
	 * Creates an empty {@link CurseFiles} instance ordered from newest to oldest.
	 */
	public CurseFiles() {
		//Default constructor.
	}

	/**
	 * Creates an empty {@link CurseFiles} instance with the specified {@link Comparator}.
	 *
	 * @param comparator a {@link Comparator}.
	 */
	public CurseFiles(Comparator<? super F> comparator) {
		super(comparator);
	}

	/**
	 * Creates a {@link CurseFiles} instance containing all of the {@link CurseFile}s in the
	 * specified collection.
	 *
	 * @param files a collection of {@link CurseFile}s.
	 */
	public CurseFiles(Collection<? extends F> files) {
		super(files);
	}

	/**
	 * Creates a {@link CurseFiles} instance containing all of the {@link CurseFile}s in the
	 * specified collection with the specified {@link Comparator}.
	 *
	 * @param files a collection of {@link CurseFile}s.
	 * @param comparator a {@link Comparator}.
	 */
	public CurseFiles(
			Collection<? extends F> files, Comparator<? super F> comparator
	) {
		super(comparator);
		addAll(files);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CurseFiles<F> clone() {
		return (CurseFiles<F>) super.clone();
	}

	/**
	 * Removes all {@link CurseFile}s in this {@link CurseFiles} that do not match the specified
	 * filter. This is done by calling {@link #remove(Object)} with the {@link Predicate}
	 * returned by calling {@link Predicate#negate()} on the specified filter.
	 *
	 * @param filter a {@link Predicate}.
	 * @return {@code true} if any elements were removed, or otherwise {@code false}.
	 */
	public boolean filter(Predicate<? super F> filter) {
		return removeIf(filter.negate());
	}

	/**
	 * Returns the {@link CurseFile} instance in this {@link CurseFiles} with the specified ID.
	 *
	 * @param id a file ID.
	 * @return the {@link CurseFile} instance in this {@link CurseFiles} with the specified ID,
	 * or {@code null} if it does not exist.
	 * @see CurseFileFilter
	 */
	@Nullable
	public F fileWithID(int id) {
		CursePreconditions.checkFileID(id, "id");

		for (F file : this) {
			if (id == file.id()) {
				return file;
			}
		}

		return null;
	}

	/**
	 * Returns a copy of this {@link CurseFiles} instance with the specified {@link Comparator}.
	 *
	 * @param comparator a {@link Comparator}.
	 * @return a copy of this {@link CurseFiles} instance with the specified {@link Comparator}.
	 */
	public CurseFiles<F> withComparator(Comparator<? super F> comparator) {
		return new CurseFiles<>(this, comparator);
	}

	/**
	 * Returns a collection derived from the elements of this {@link CurseFiles} by applying the
	 * specified mapping function.
	 * <p>
	 * The advantage of this method over the traditional {@link java.util.stream.Stream}
	 * methods is that it uses {@link CheckedFunction}s rather than regular {@link Function}s,
	 * and this allows methods that throw {@link CurseException}s such as
	 * {@link CurseFile#changelog()} to be called.
	 * <p>
	 * {@link #parallelStream()} is used to iterate over elements of this {@link CurseFiles},
	 * meaning that time-consuming requests may be executed in parallel.
	 *
	 * @param function a {@link CheckedFunction} that maps files to objects of type {@code R}.
	 * @param collector a {@link Collector}.
	 * @param <R> the type of the resultant values.
	 * @param <C> the type of the resultant collection.
	 * @return a collection derived from the elements of this {@link CurseFiles}.
	 * @throws CurseException if an error occurs.
	 * @see CurseAPI#parallelMap(Collection, CheckedFunction, Collector)
	 */
	public <R, C> C parallelMap(
			CheckedFunction<? super F, ? extends R, CurseException> function,
			Collector<? super R, ?, C> collector
	) throws CurseException {
		return CurseAPI.parallelMap(this, function, collector);
	}

	/**
	 * Returns a {@link Map} derived from the elements of this {@link CurseFiles}.
	 * <p>
	 * The advantage of this method over the traditional {@link java.util.stream.Stream}
	 * methods is that it uses {@link CheckedFunction}s rather than regular {@link Function}s,
	 * and this allows methods that throw {@link CurseException}s such as
	 * {@link CurseFile#changelog()} to be called.
	 * <p>
	 * The key function and the value function are both called on each {@link CurseFile}
	 * to retrieve the keys and values of the {@link Map} respectively. Additionally,
	 * {@link #parallelStream()} is used to iterate over elements of this {@link CurseFiles},
	 * meaning that time-consuming requests may be executed in parallel.
	 *
	 * @param keyMapper a {@link CheckedFunction} that maps files to objects of type {@code K}.
	 * @param valueMapper a {@link CheckedFunction} that maps files to objects of type {@code V}.
	 * @param <K> the type of the keys.
	 * @param <V> the type of the values.
	 * @return a {@link Map} derived from the elements of this {@link CurseFiles}.
	 * @throws CurseException if an error occurs.
	 * @see CurseAPI#parallelMap(Collection, CheckedFunction, CheckedFunction)
	 */
	public <K, V> Map<K, V> parallelMap(
			CheckedFunction<? super F, ? extends K, CurseException> keyMapper,
			CheckedFunction<? super F, ? extends V, CurseException> valueMapper
	) throws CurseException {
		return CurseAPI.parallelMap(this, keyMapper, valueMapper);
	}

	/**
	 * Returns a {@link Collector} that accumulates the input elements into a
	 * new {@link CurseFiles}.
	 *
	 * @param <F> the type of {@link BasicCurseFile}.
	 * @return a {@link Collector} that accumulates the input elements into a
	 * new {@link CurseFiles}.
	 */
	public static <F extends BasicCurseFile> Collector<F, ?, CurseFiles<F>> toCurseFiles() {
		return Collectors.toCollection(CurseFiles::new);
	}
}
