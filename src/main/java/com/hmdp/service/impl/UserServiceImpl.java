package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Created by IntelliJ IDEA.
     *
     * @param phone
     * @param session
     * @return com.hmdp.dto.Result
     * @author ZhuShang
     * @date 2023/8/3 15:35
     * @Description 验证码生成与发送
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号码格式错误!");
        }
        String code = RandomUtil.randomNumbers(6);

        /**Seession实现验证码存储与过期
         session.setAttribute("code", code);
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
        @Override public void run() {
        session.removeAttribute("code"); // 移除字段
        log.warn("验证码已过期,Session已移除验证码:{}",code);
        }
        }, 300 * 1000);*/
        //TODO Redis实现验证码存储与过期
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("已成功发送短信验证码,验证码:{}", code);
        return Result.ok();
    }

    /**
     * Created by IntelliJ IDEA.
     *
     * @param loginForm
     * @param session
     * @return com.hmdp.dto.Result
     * @author ZhuShang
     * @date 2023/8/4 8:57
     * @Description 登录校验
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            return Result.fail("手机号码格式错误!");
        }
        /**Session实现登录校验
         Object cacheCode = session.getAttribute("code");
         String code = loginForm.getCode();
         if (cacheCode == null){
         return Result.fail("验证码已过期,请重新获取!");
         }
         if (!cacheCode.toString().equals(code)){
         return Result.fail("验证码错误!");
         }
         User user = query().eq("phone", loginForm.getPhone()).one();
         if (user == null){
         user = createUserByPhone(loginForm.getPhone());
         }
         session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
         */
        //TODO Redis实现登录校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + loginForm.getPhone());
        String code = loginForm.getCode();
        if (cacheCode == null) {
            return Result.fail("验证码已过期,请重新获取!");
        }
        if (!cacheCode.equals(code)) {
            return Result.fail("验证码错误!");
        }
        User user = query().eq("phone", loginForm.getPhone()).one();
        if (user == null) {
            user = createUserByPhone(loginForm.getPhone());
        }
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 解决UserDTO字段ID值类型为Long,StringRedisTemplate要求字段类型String的转换异常
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions
                .create().setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return Result.ok(token);
    }

    private User createUserByPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
