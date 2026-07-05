package kr.ktb.finn_week6.security.auth;

import kr.ktb.finn_week6.global.RequestMessage;
import kr.ktb.finn_week6.global.customException.UnauthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LoginUserProvider {


    public Long getLoginUserId(){
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new UnauthenticatedException(RequestMessage.UNAUTHORIZED.getDescription());
        }

        if(!(authentication.getPrincipal() instanceof Long)){
            throw new UnauthenticatedException(RequestMessage.UNAUTHORIZED.getDescription());
        }
        return (Long) authentication.getPrincipal();
    }


}
