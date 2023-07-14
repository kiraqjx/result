/*
 * Copyright 2022-2032 the original author or authors.
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

package com.github.kiraqjx.result;

import com.github.kiraqjx.error.BaseError;
import com.github.kiraqjx.error.WrapRuntimeException;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

/**
 * Error handling
 *
 * @author kiraqjx
 * @param <T> data
 * @param <E> error
 */
public class Result<T, E extends BaseError> implements Serializable {
    private static final long serialVersionUID = 2078916435339471237L;

    private final T data;
    private final E error;

    public Result(T data) {
        this.data = data;
        this.error = null;
    }

    public Result(E error) {
        this.data = null;
        this.error = error;
    }

    public <U> U match(Function<T, U> function, Function<E, U> errorFunction) {
        return this.mapOrElse(errorFunction, function);
    }

    // Adapter for each variant

    public Optional<T> ok() {
        return this.data == null ? Optional.empty() : Optional.of(this.data);
    }

    public Optional<E> err() {
        return this.error == null ? Optional.empty() : Optional.of(this.error);
    }

    // Querying the contained values

    public boolean isOk() {
        return this.error == null;
    }

    public boolean isOk(Function<T, Boolean> function) {
        return !isError() && function.apply(this.data);
    }

    public boolean isError() {
        return this.error != null;
    }

    public boolean isError(Function<E, Boolean> function) {
        return !isOk() && function.apply(this.error);
    }

    // Extract a value

    public void expect() {
        if (this.error != null) {
            throw new WrapRuntimeException(this.error);
        }
    }

    public T unwrap() {
        this.expect();
        return this.data;
    }

    // Transforming contained values

    public <U> Result<U, E> map(Function<T, U> function) {
        return new Result<>(function.apply(this.data));
    }

    public <U> U mapOr(U defaultValue, Function<T, U> function) {
        return this.error == null ? function.apply(this.data) : defaultValue;
    }

    public <U> U mapOrElse(Function<E, U> defaultValue, Function<T, U> function) {
        return this.error == null ? function.apply(this.data) : defaultValue.apply(this.error);
    }

    public <F extends BaseError> Result<T, F> mapErr(Function<E, F> function) {
        return new Result<>(function.apply(this.error));
    }

    public Result<T, E> inspect(Function<T, Void> function) {
        if (isOk()) {
            function.apply(this.data);
        }
        return this;
    }

    public Result<T, E> inspectErr(Function<E, Void> function) {
        if (isError()) {
            function.apply(this.error);
        }
        return this;
    }

    // Boolean operations on the values, eager and lazy
    public <U> Result<U, E> and(Result<U, E> result) {
        return isOk() ? result : new Result<>(this.error);
    }

    public <U> Result<U, E> andThen(Function<T, Result<U, E>> function) {
        return isOk() ? function.apply(this.data) : new Result<>(this.error);
    }

    public <F extends BaseError> Result<T, F> or(Result<T, F> result) {
        return isOk() ? new Result<>(this.data) : result;
    }

    public <F extends BaseError> Result<T, F> orElse(Function<E, Result<T, F>> function) {
        return isOk() ? new Result<>(this.data) : function.apply(this.error);
    }

    public T unwrapOr(T defaultValue) {
        return isOk() ? this.data : defaultValue;
    }

    public T unwrapOrElse(Function<E, T> function) {
        return isOk() ? this.data : function.apply(this.error);
    }
}
