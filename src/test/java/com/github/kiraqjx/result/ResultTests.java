package com.github.kiraqjx.result;

import com.github.kiraqjx.error.BaseError;
import com.github.kiraqjx.error.WrapRuntimeException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultTests {

    @Test
    public void testResultSuccess() {
        Result<Integer, BaseError> result = new Result<>(1);
        assert result.ok().isPresent();
        assert result.ok().get().equals(1);
        assert result.isOk();
        assert result.isOk(value -> value.equals(1));
        assert result.unwrap().equals(1);
    }

    @Test
    public void testResultError() {
        String errMsg = "测试错误";
        Result<Integer, BaseError> result = new Result<>(new BaseError(errMsg) {});
        assert result.err().isPresent();
        assert result.err().get().getMsg().equals(errMsg);
        assert result.isError();
        assert result.isError(error -> error.getMsg().equals(errMsg));
        try {
            result.unwrap();
        } catch (WrapRuntimeException wrapRuntimeException) {
            assert wrapRuntimeException.getBaseError().getMsg().equals(errMsg);
            assert wrapRuntimeException.getMsg().equals(errMsg);
        }
        try {
            result.expect();
        } catch (WrapRuntimeException wrapRuntimeException) {
            assert wrapRuntimeException.getMsg().equals(errMsg);
        }
    }

    @Test
    public void testTransforming() {
        String errMsg = "测试错误";
        String errMsgO = "测试错误1";
        Result<Integer, BaseError> result = new Result<>(1);
        Result<Integer, BaseError> errResult = new Result<>(new BaseError(errMsg) {});

        assert result.map(value -> value + 1).unwrap().equals(2);
        assert errResult.mapOr(2, value -> value + 1).equals(2);
        assert result.match(value -> value + 1, BaseError::getMsg).equals(2);
        assert result.mapOrElse(BaseError::getMsg, value -> value + 1).equals(2);
        assert errResult.mapOrElse(BaseError::getMsg, value -> value + 1).equals(errMsg);
        try {
            errResult.mapErr(err -> new BaseError(errMsgO) {}).expect();
        } catch (WrapRuntimeException wrapRuntimeException) {
            assert wrapRuntimeException.getMsg().equals(errMsgO);
        }

        result.inspect(value -> {
            assert true;
            return null;
        });
        errResult.inspect(value -> {
            assert false;
            return null;
        });

        errResult.inspectErr(err -> {
            assert true;
            return null;
        });
        result.inspectErr(err -> {
            assert false;
            return null;
        });

        assert result.unwrapOr(2).equals(1);
        assert errResult.unwrapOr(2).equals(2);

        assert result.unwrapOrElse(error -> 2).equals(1);
        assert errResult.unwrapOrElse(error -> 2).equals(2);
    }

    @Test
    public void testLogic() {
        String errMsg = "测试错误";
        String errMsgO = "测试错误1";
        Result<Integer, BaseError> result = new Result<>(1);
        Result<Integer, BaseError> errResult = new Result<>(new BaseError(errMsg) {});
        Result<Integer, BaseError> resultSuc = new Result<>(2);
        Result<Integer, BaseError> errResultO = new Result<>(new BaseError(errMsgO) {});

        // and
        assert result.and(resultSuc).unwrap().equals(2);
        try {
            result.and(errResult).expect();
        } catch (WrapRuntimeException wrapRuntimeException) {
            assert wrapRuntimeException.getMsg().equals(errMsg);
        }
        try {
            errResult.and(result).expect();
        } catch (WrapRuntimeException wrapRuntimeException) {
            assert wrapRuntimeException.getMsg().equals(errMsg);
        }
        try {
            errResult.and(errResultO).expect();
        } catch (WrapRuntimeException wrapRuntimeException) {
            assert wrapRuntimeException.getMsg().equals(errMsg);
        }

        // andThen
        assert result.andThen(value -> new Result<>(2)).unwrap().equals(2);
        try {
            errResult.andThen(value -> new Result<>(2)).expect();
        } catch (WrapRuntimeException wrapRuntimeException) {
            assert wrapRuntimeException.getMsg().equals(errMsg);
        }

        // or
        assert result.or(resultSuc).unwrap().equals(1);
        assert result.or(errResult).unwrap().equals(1);
        assert errResult.or(result).unwrap().equals(1);
        try {
            errResult.or(errResultO).expect();
        } catch (WrapRuntimeException wrapRuntimeException) {
            assert wrapRuntimeException.getMsg().equals(errMsgO);
        }

        // or else
        assert result.orElse(err -> new Result<>(new BaseError(errMsgO) {})).unwrap().equals(1);
        try {
            errResult.orElse(err -> new Result<>(new BaseError(errMsgO) {})).expect();
        } catch (WrapRuntimeException wrapRuntimeException) {
            assert wrapRuntimeException.getMsg().equals(errMsgO);
        }
    }

    @Test
    public void testResultMapGenerics() {
        Result<Integer, BaseError> result = new Result<>(1);
        Result<List<Integer>, BaseError> result1 = result.map(Arrays::asList);
        Result<Map<String, Integer>, BaseError> result2 = result.map(value -> {
            Map<String, Integer> map = new HashMap<>(2);
            map.put("test", value);
            return map;
        });
        assert result1.unwrap().get(0).equals(1);
        assert result2.unwrap().get("test").equals(1);
    }

}
