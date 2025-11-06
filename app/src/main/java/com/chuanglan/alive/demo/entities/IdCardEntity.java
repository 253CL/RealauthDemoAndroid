package com.chuanglan.alive.demo.entities;

public class IdCardEntity {

    private String code;
    private String message;
    private DataBean data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private BackBean back;
        private FrontBean front;

        public BackBean getBack() {
            return back;
        }

        public void setBack(BackBean back) {
            this.back = back;
        }

        public FrontBean getFront() {
            return front;
        }

        public void setFront(FrontBean front) {
            this.front = front;
        }

        public static class BackBean {
            private Object address;
            private Object id_card_no;
            private Object brith_day;
            private Object name;
            private Object sex;
            private Object nation;
            private String issuing_authority;
            private String issuing_date;
            private String expire_date;
            private Object msg;

            public Object getAddress() {
                return address;
            }

            public void setAddress(Object address) {
                this.address = address;
            }

            public Object getId_card_no() {
                return id_card_no;
            }

            public void setId_card_no(Object id_card_no) {
                this.id_card_no = id_card_no;
            }

            public Object getBrith_day() {
                return brith_day;
            }

            public void setBrith_day(Object brith_day) {
                this.brith_day = brith_day;
            }

            public Object getName() {
                return name;
            }

            public void setName(Object name) {
                this.name = name;
            }

            public Object getSex() {
                return sex;
            }

            public void setSex(Object sex) {
                this.sex = sex;
            }

            public Object getNation() {
                return nation;
            }

            public void setNation(Object nation) {
                this.nation = nation;
            }

            public String getIssuing_authority() {
                return issuing_authority;
            }

            public void setIssuing_authority(String issuing_authority) {
                this.issuing_authority = issuing_authority;
            }

            public String getIssuing_date() {
                return issuing_date;
            }

            public void setIssuing_date(String issuing_date) {
                this.issuing_date = issuing_date;
            }

            public String getExpire_date() {
                return expire_date;
            }

            public void setExpire_date(String expire_date) {
                this.expire_date = expire_date;
            }

            public Object getMsg() {
                return msg;
            }

            public void setMsg(Object msg) {
                this.msg = msg;
            }
        }

        public static class FrontBean {
            private String address;
            private String id_card_no;
            private String brith_day;
            private String name;
            private String sex;
            private String nation;
            private Object issuing_authority;
            private Object issuing_date;
            private Object expire_date;
            private Object msg;

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getId_card_no() {
                return id_card_no;
            }

            public void setId_card_no(String id_card_no) {
                this.id_card_no = id_card_no;
            }

            public String getBrith_day() {
                return brith_day;
            }

            public void setBrith_day(String brith_day) {
                this.brith_day = brith_day;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSex() {
                return sex;
            }

            public void setSex(String sex) {
                this.sex = sex;
            }

            public String getNation() {
                return nation;
            }

            public void setNation(String nation) {
                this.nation = nation;
            }

            public Object getIssuing_authority() {
                return issuing_authority;
            }

            public void setIssuing_authority(Object issuing_authority) {
                this.issuing_authority = issuing_authority;
            }

            public Object getIssuing_date() {
                return issuing_date;
            }

            public void setIssuing_date(Object issuing_date) {
                this.issuing_date = issuing_date;
            }

            public Object getExpire_date() {
                return expire_date;
            }

            public void setExpire_date(Object expire_date) {
                this.expire_date = expire_date;
            }

            public Object getMsg() {
                return msg;
            }

            public void setMsg(Object msg) {
                this.msg = msg;
            }
        }
    }
}
