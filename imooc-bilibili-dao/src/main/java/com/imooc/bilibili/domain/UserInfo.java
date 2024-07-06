package com.imooc.bilibili.domain;

import java.util.Date;

public class UserInfo {

        private Long id;
        private Long userId;
        private String nick;
        private String avatar;
        private String sign;
        private String gender;
        private String birth;
        private Date createtime;
        private Date updatetime;
        private boolean followed;

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public Long getUserId() {
                return userId;
        }

        public void setUserId(Long userId) {
                this.userId = userId;
        }

        public String getNick() {
                return nick;
        }

        public void setNick(String nick) {
                this.nick = nick;
        }

        public String getAvatar() {
                return avatar;
        }

        public void setAvatar(String avatar) {
                this.avatar = avatar;
        }

        public String getSign() {
                return sign;
        }

        public void setSign(String sign) {
                this.sign = sign;
        }

        public String getGender() {
                return gender;
        }

        public void setGender(String gender) {
                this.gender = gender;
        }

        public String getBirth() {
                return birth;
        }

        public void setBirth(String birth) {
                this.birth = birth;
        }

        public Date getCreatetime() {
                return createtime;
        }

        public void setCreatetime(Date createtime) {
                this.createtime = createtime;
        }

        public Date getUpdatetime() {
                return updatetime;
        }

        public void setUpdatetime(Date updatetime) {
                this.updatetime = updatetime;
        }

    public void setFollowed(boolean b) {
                this.followed=b;
    }
}
