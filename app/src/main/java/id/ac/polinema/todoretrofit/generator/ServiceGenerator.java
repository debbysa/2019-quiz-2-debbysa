package id.ac.polinema.todoretrofit.generator;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.voice.VoiceInteractionSessionService;
import android.util.Log;

import java.io.IOException;

import id.ac.polinema.todoretrofit.Application;
import id.ac.polinema.todoretrofit.Constant;
import id.ac.polinema.todoretrofit.Session;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

	//	private static SharedPreferences preferences = Application.getPreferences();
	private static Session session = Application.provideSession();

	private static final String BASE_URL = "http://192.168.43.199:3000/";

	private static Retrofit.Builder builder = new Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create());

	private static Retrofit retrofit = builder.build();

	private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
			.setLevel(HttpLoggingInterceptor.Level.BODY);

	private static ApiInterceptor apiInterceptor = new ApiInterceptor();

	private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

	private ServiceGenerator() {}

	public static <Service> Service createService(Class<Service> serviceClass) {
		if (!httpClient.interceptors().contains(logging)) {
			httpClient.addInterceptor(logging);
		}
		if (!httpClient.interceptors().contains(apiInterceptor)) {
			httpClient.addInterceptor(apiInterceptor);
		}
		builder.client(httpClient.build());
		retrofit = builder.build();
		return retrofit.create(serviceClass);
	}

	static class ApiInterceptor implements Interceptor {

		@Override
		public Response intercept(Chain chain) throws IOException {
			Request request = chain.request();
			if (request.header("No-Authentication") == null) {
				String token = session.getToken();
				request = request.newBuilder()
						.addHeader("Authorization", "Bearer " + token)
						.build();
			}

			return chain.proceed(request);
		}
	}
}
