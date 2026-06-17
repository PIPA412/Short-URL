package com.shortlink.service;

import com.shortlink.dto.LoginDTO;
import com.shortlink.dto.LoginVO;
import com.shortlink.dto.RegisterDTO;

public interface UserService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);
}
