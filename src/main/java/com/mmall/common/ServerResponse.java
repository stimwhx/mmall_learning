package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
//Serializable，是序列接口

/***
 *
 * @param <T>代表响应里的类型
 *           这个类在返回的时候会进行序列化，然后再返回给前端
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//保证序列化json时如果values是null,那么key也会消失
public class ServerResponse<T> implements Serializable {
    //这里定义的三个方法，每一个响应都能用到，所以放在公共类里了
    private int status;
    private String msg;
    private T data;
    //保证序列化json时如果values是null,那么key也会消失，像这个只返回一个状态 ，没有data和msg说明两个为null那么就没有必要返回
    private ServerResponse(int status){
        this.status=status;
    }
    private ServerResponse(int status ,T data){
        this.status=status;
        this.data=data;
    }
    private ServerResponse(int status,String msg){
        this.status=status;
        this.msg=msg;
    }
    private ServerResponse(int status,String msg,T data){
        this.status=status;
        this.msg=msg;
        this.data=data;
    }
    @JsonIgnore
    //在序列化之后不会展示 在json里面
public boolean isSuccess(){
        return status==ResponseCode.SUCCESS.getCode();
}
public int getStatus(){
        return status;
}
public T getData(){
        return data;
}
public String getMsg(){
        return msg;
}
public static <T> ServerResponse<T> createrBySuccess(){
        return new ServerResponse(ResponseCode.SUCCESS.getCode());
}
public static <T> ServerResponse<T> createrBySuccessMsg(String msg){
        return new ServerResponse(ResponseCode.SUCCESS.getCode(),msg);
}
public static  <T> ServerResponse<T> createrBySuccess(T data){
        return new ServerResponse(ResponseCode.SUCCESS.getCode(),data);
}
    public static  <T> ServerResponse<T> createrBySuccess(String msg,T data){
        return new ServerResponse(ResponseCode.SUCCESS.getCode(),msg,data);
    }
public static <T> ServerResponse<T> createrByError(){
        return new ServerResponse(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
}
public static <T> ServerResponse<T> createrByErrorMsg(String msg){
        return new ServerResponse(ResponseCode.ERROR.getCode(),msg);
}

public static <T> ServerResponse<T> createrByErrorCodeMsg(int errorCode,String errorMsg){
        return new ServerResponse(errorCode,errorMsg);
}
}
