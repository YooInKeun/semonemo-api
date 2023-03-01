package semonemo.config

import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import semonemo.model.exception.UnauthorizedException

@Component
class LoginUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(LoginUser::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any> {
        return exchange.session
            .mapNotNull<Any?> { it.getAttribute(LOGIN_ATTRIBUTE_NAME) }
            .switchIfEmpty { Mono.error(UnauthorizedException("로그인이 필요합니다.")) }
    }

    companion object {
        const val LOGIN_ATTRIBUTE_NAME = "LOGIN_USER"
    }
}
