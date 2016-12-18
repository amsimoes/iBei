package sd.interceptor;
import java.util.Map;
import java.util.Calendar;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class CounterInterceptor implements Interceptor {
	private static final long serialVersionUID = 189237412378L;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		Map<String, Object> session = invocation.getInvocationContext().getSession();
		
		// this method intercepts the execution of the action and we get access
		// to the session, to the action, and to the context of this invocation
		
		session.put("detail_id", 0);
		System.out.println("counter");
		return Action.SUCCESS;
	}

	@Override
	public void init() { }
	
	@Override
	public void destroy() { }
}
