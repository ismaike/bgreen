package io.github.model;

/**
 * @author maike
 * @date 2021年12月07日 10:45 下午
 */
public class Result<T> {

    /**
     * 成功
     */
    private static final int SUCCESS_CODE = 0;
    /**
     * 系统繁忙,请稍后重试
     */
    private static final int FAIL_CODE = -1;


    /**
     * 返回结果数据
     */
    private T data;

    /**
     * 0:成功，-1：失败
     */
    private int code;

    /**
     * 错误内容
     */
    private String msg;

    public Result() {
        this.code = SUCCESS_CODE;
    }

    public Result(T data) {
        this.code = SUCCESS_CODE;
        this.data = data;
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static Result success() {
        return new Result();
    }

    public static <E> Result<E> success(E data) {
        return new Result(data);
    }

    public static <E> Result<E> success(E data, String msg) {
        return new Result(SUCCESS_CODE, data, msg);
    }

    public static <E> Result<E> result(int code, E data, String msg) {
        return new Result(code, data, msg);
    }

    public static <E> Result<E> fail(String msg) {
        return new Result<>(FAIL_CODE, msg);
    }

    /**
     * true :成功  false ：失败
     *
     * @return
     */
    public boolean getSuccess() {
        return code == SUCCESS_CODE;
    }

    public String getMsg() {
        return msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public int getCode() {
        return code;
    }

    public Result<T> setCode(int code) {
        this.code = code;
        return this;
    }
}
