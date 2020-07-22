package com.tencent.miaosha.service;

import com.tencent.miaosha.dao.UserDao;
import com.tencent.miaosha.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired(required = false)
    UserDao userDao;

    public User getById(int id){
        return userDao.getById(id);
    }

   // @Transactional
    public boolean tx() {
        User u1 = new User();
        u1.setId(5);
        u1.setName("alihbquan");
        userDao.insert(u1);

        /*User u2 = new User();
        u2.setId(2);
        u2.setName("quanhangbo");
        userDao.insert(u2);*/
        return true;
    }


}
