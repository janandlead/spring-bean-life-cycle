package com.durga.spring_bean_life_cycle;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class Employee implements InitializingBean, DisposableBean, BeanNameAware, BeanPostProcessor{
	
	
	@PostConstruct
	public void intialize() {
		//Add any kind of initilization logic 
		// cache activity
		System.out.println(">>> @PostConstruct executed <<<<<<");
	}

	
	@PreDestroy
	public void cleanUp() {
		//clear/stop all the running jobs or scheduar jobs
		System.out.println(">>> @PreDestroy executed <<<<<<");
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception{
		System.out.println(">>> afterPropertiesSet executed <<<<<<");
	}
	
	public void destroy() throws Exception{
		System.out.println(">>> destroy executed <<<<<<");
	}
	
	public void setBeanName(String name) {
		System.out.println("Bean Name : "+name);
	
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		System.out.println(">>> postProcessBeforeInitialization executed for bean: " + beanName + " <<<<<<");
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		System.out.println(">>> postProcessAfterInitialization executed for bean: " + beanName + " <<<<<<");
		return bean;
	}

}
