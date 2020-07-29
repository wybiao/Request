# Request

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  Step 2. Add the dependency
  
  	dependencies {
	        implementation 'com.github.wybiao.Request:requestlibrary:1.0.4'
          annotationProcessor 'com.github.wybiao.Request:requestcomplier:1.0.4'
	}
  
  使用方法：
首先进行初始化，可传入域名或者ip , 也就是请求地址共用的部分

    RequestManager.init(this, "https:123"); // 初始化

然后实现请求类,继承BaseRequest

  public class TestRequest extends BaseRequest {

      @FieldName("mobile")
      public String mobile;
      @FieldName("password")
      public String password;
      @FieldName("file")
      public PostFileInfo file;

      public TestRequest(String mobile, String password, PostFileInfo file) {
          this.mobile = mobile;
          this.password = password;
          this.file = file;
      }

      @Override
      public String getUrl() {
  //        return "/api/login/login";
          return "http://123/api/login/login";
      }
  }
  
  请求类，可以在定义完请求参数后，通过我的插件生成注解和构造方法：https://github.com/wybiao/RequestPlugin
  
  具体请求方式为：

    RequestManager.getInstance().sendRequest(new TestRequest("adsfsadf", "999", PostFileUtils.getPostFileByUri(this, Uri.fromFile(new File("xxxx")))), String.class, false, new OnRequestListener() {
                @Override
                public void onRequestError(BaseRequest request, BaseResponse response) {

                }

                @Override
                public void onRequestSuccessful(BaseRequest request, BaseResponse response) {
                      String aa = (String) response.getData();
                }
            });
  
  服务器返回数据有一些固定样式：code ，msg ,data 为返回的数据结构，也就是下面请求参数clazz对应的结构。请求成功后response.getData()就是想要的具体数据了
    {
        code: 200,
        msg: "成功",
        data: {
            id: 2,
            username: "sldjf"
            type: 2
        }
    }
  
    /**
     * @param baseRequest 请求reueqest
     * @param clazz       返回的数据类型
     * @param isList      返回的数据是否是数组
     * @param listener    请求回调的接口
     */
    public void sendRequest(final BaseRequest baseRequest, final Class clazz, final boolean isList, OnRequestListener listener) {}
    
    
