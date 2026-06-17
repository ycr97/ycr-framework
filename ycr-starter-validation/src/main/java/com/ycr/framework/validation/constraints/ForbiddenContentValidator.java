package com.ycr.framework.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * {@link ForbiddenContent} 校验器：检测字符串是否含 XSS / SQL / 爬虫 / python 代码特征。
 *
 * <p>检测规则改写自 middle-common 的 TextCheckUtils；null 或空白视为合法。</p>
 *
 * @author ycr
 */
public class ForbiddenContentValidator implements ConstraintValidator<ForbiddenContent, String> {

    private static final Pattern XSS = Pattern.compile(
            "(<script.*?>.*?</script>)|((?:javascript):)|(<(?!video)[^>]*?on[a-zA-Z]*\\s*=.*?)|(<iframe.*?>.*?</iframe>)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern SPIDER = Pattern.compile(
            "(curl|wget|httpclient|urllib|scrapy|bot|spider|crawler)", Pattern.CASE_INSENSITIVE);

    private static final Pattern SQL = Pattern.compile(
            "(?i)\\b(SELECT|INSERT|DELETE|UPDATE|DROP|CREATE|ALTER|TRUNCATE|EXEC|UNION|GRANT|REVOKE|DECLARE)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PYTHON = Pattern.compile(
            "(import\\s+\\w+|def\\s+\\w+|class\\s+\\w+|print\\s*\\(|exec\\s*\\(|eval\\s*\\()",
            Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !(XSS.matcher(value).find()
                || SQL.matcher(value).find()
                || SPIDER.matcher(value).find()
                || PYTHON.matcher(value).find());
    }
}
