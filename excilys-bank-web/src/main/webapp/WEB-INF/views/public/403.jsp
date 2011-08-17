<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="bk" uri="http://www.excilys.com/jsp/jstl/bank"%>
<div class="sw">
	<div class="sw_icon icon users"></div>
	<div class="sw_header"><spring:message code="403.message" /></div>
	<div class="sw_content">
		<a href="${bk:ctx()}/public/login.html"><spring:message code="common.back" /></a>
	</div>
	<div class="sw_footer"></div>
</div>