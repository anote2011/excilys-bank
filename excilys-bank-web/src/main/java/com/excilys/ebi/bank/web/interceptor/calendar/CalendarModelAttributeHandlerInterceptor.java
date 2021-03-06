/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.bank.web.interceptor.calendar;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import com.excilys.ebi.bank.service.BankService;
import com.excilys.ebi.bank.web.interceptor.AnnotatedMethodHandlerInterceptor;

public class CalendarModelAttributeHandlerInterceptor extends AnnotatedMethodHandlerInterceptor<CalendarModelAttribute> {

	@Autowired
	protected BankService bankService;

	public CalendarModelAttributeHandlerInterceptor() {
		super(CalendarModelAttribute.class);
	}

	@Override
	protected void postHandleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, ModelAndView modelAndView, Map<String, ?> pathVariables)
			throws Exception {
		exportCalendar(modelAndView.getModelMap(), pathVariables);
	}

	private void exportCalendar(ModelMap model, Map<String, ?> pathVariables) {
		Integer year = getModelOrPathAttribute("year", model, pathVariables);
		Integer month = getModelOrPathAttribute("month", model, pathVariables);

		model.addAttribute("calendar", bankService.getCalendar(year, month));
	}
}
