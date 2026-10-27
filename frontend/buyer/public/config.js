var BASE = {
  /**
   * @description api请求基础路径
   */
  API_DEV: {
    buyer: "https://buyer-api.pickmall.cn",
    /**
     * @description 网关地址：AI 助手等所有需要经网关转发的接口都走这里
     */
    gateway: "http://localhost:9000",
  },
  API_PROD: {
    buyer: "https://buyer-api.pickmall.cn",
    gateway: "http://localhost:9000",
  },
};
