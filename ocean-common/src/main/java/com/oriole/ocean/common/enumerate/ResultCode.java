package com.oriole.ocean.common.enumerate;


import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(true, 1, "操作成功"),
    /* User 业务错误-2 序号1 */

    /* User 泛业务错误-2 序号1 子序号9*/
    UNAUTHORIZED_OPERATION(false, -2190, "用户操作非法（无权操作）。"),

    /* UserBehavior 业务错误-2 序号1 子序号8*/
    USER_BEHAVIOR_REPEAT(false, -2200, "用户已进行过该行为，不能重复进行。"),

    /* DocsService 业务错误-2 序号3 */
    DOCS_NOT_APPROVED(false, -2301, "文档暂未通过审核，无法获取。"),
    DOCS_NOT_ALLOW_ANON_GET(false, -2302, "文档不能匿名访问，请登录后获取。"),
    DOCS_NOT_ALLOW_SELF_EVALUATION(false, -2303, "用户不能评价自己的文档。");

    // 响应是否成功
    private final Boolean success;
    // 响应状态码
    private final Integer code;
    // 响应信息
    private final String msg;

    ResultCode(boolean success, Integer code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }
}