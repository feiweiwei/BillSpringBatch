
## 前言

最近项目要做聚合支付，聚合支付顾名思义就是将市面上常用的三方支付进行聚合，这样开发者只需要对接我们一方就可以同时对接支付宝、微信支付等其他第三方支付平台，省去了挨个平台对接调试的时间，既然支付给人家聚合了，那么必然要有个配套的功能那就是聚合账单，如果要做聚合账单就意味着需要分别从不同的三方渠道获取到商户的账单数据，再所有渠道该商户的对账单数据聚合成一份账单并进行加工成统一的格式方便接入方进行对账，这个功能还是能很好的戳中接入方财务人员的痛点的，如果没有聚合对账，那么意味着财务需要对所有平台的账单进行分别下载再分别对账；

好了，说了那么多，我们这里要解决的问题就是批量将下载下来的商户账单进行加载，加载后统一处理后再输出聚合账单，这就涉及到一个问题，需要有个批量系统进行对数据进行处理，下面就开始我们今天的主角Spring Batch。

## 1. 为什么要用Spring Batch

为什么要用Spring Batch？回答这个问题那就要先想明白，我们遇到了什么问题，在前面说了，我们需要做一个账单批量加工的功能，这里就需要一个框架有以下功能：

1）能够定义不同job之间的依赖关系，这样可以进行不同job的顺序调整了和依赖关系设定；

2）能够定义job中的步骤，并且能够对每个步骤的输入、输出数据进行操作；

3）能够支持对多种数据源进行操作，不但能批量操作文件，也能够对数据库进行批量操作；

4）对于批量异常数据能够有接口进行自定义处理；

5）支持对job进行重跑；

6）支持对job失败后自动重试等容错处理机制；

7）支持对每个job的输入数据源进行校验；

上面罗列了下实现一个批量账单加工功能需要有上面这些基础功能，做过批量的同学一看这些基础功能，就会发出感叹，这不就是一个批量框架最基础的功能吗？但是目前opensource社区，确实这类批量框架很少，之前在国有银行里面，我们用的框架是自研框架，因为市面上不管是收费的还是开源的批量产品都很难满足我们的功能和性能方面的要求，而且据我了解，大部分有大型批量系统的功能用的批量框架也都是自研框架，很少用现成的产品或者开源的批量产品，因为笔者目前所在的银行规模很小，根本没有自研的能力，核心系统、数仓这些系统也都是买来的，用的批量产品也没法直接拿来用，既然这样就干脆找个开源的批量框架来解决问题，对比后最后选择的是Spring Batch，一方面是他拥有上面提到的所有功能，另一方面，新版本的SpringBatch可以很好的和SpringBoot结合起来，方便后面迁移到Cloud上。

## 2. Spring Batch架构

SpringBatch的github地址：<https://github.com/spring-projects/spring-batch>

SpringBatch JavaDoc地址：<https://docs.spring.io/spring-batch/trunk/apidocs/overview-summary.html>



下面先介绍下SpringBatch框架里用到的名词，然后再介绍下他们之间的关系。

**Job**： 是 Spring Batch 的核心概念，它包含了批处理的所有操作；

**Step**：每个 Job 由一个或多个 Step 组成，每个Step中涉及到ItemReader、ItemProcessor、ItemWriter，这三个接口顾名思义，一个负责数据源读取、一个负责业务逻辑处理、一个负责处理后的数据输出；

**jobRepository**：定义 Job 时，需要指定一个 JobRepository，用来存储 Job 在运行过程中的状态信息，为什么要存储状态信息呢？因为如果 Job 失败了，Spring 支持从失败的地方重新运行，而不是从头开始。

**JobLauncher**：很好理解launcher是用来执行Job的，如果不设置，系统也会默认给Job配置一个默认Launcher；

![Screenshot 2017-11-28 17.08.20.png](http://upload-images.jianshu.io/upload_images/4720632-6fc9ccc6ecb8ab14.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


Spring Batch 架构主要分为三类高级组件 : 应 层(Application),   核心层(Core) ，基础架构层(Infrastructure)
应 层层(Application)包括开发人员用SpringBatch编写的所有批处理作业和自定义代码；
Batch  (Batch Core) 包含加载和控制批处理作业所必须的核心类，包括JobLaunch，Job，Step的实现；
应 层(Application) 与核心 (Core) 都是构建在基础架构层之上，基础架构层包含readers(ItemReader)   writers(ItemWriter),    services ( 如重试模块RetryTemplate), 可以被应用层和核心层所使用。

![Screenshot 2017-11-28 16.56.05.png](http://upload-images.jianshu.io/upload_images/4720632-e4b8357786d5b183.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


介绍了SpringBatch的整体架构和组件后，下面介绍下开发者要做的内容，借助于SpringBatch已经实现了大部分的基础功能，作为开发者要做的除了配置每个Step之间的依赖关系和Job的一些参数就是开发每一步的Step，Step是一个独立封装域对象，包含了所有定义和控制实际处理信息批量任务的序列。每一个Step都是开发者自己编写的，一个Step的简单或复杂完全取决于开发者，可以把一个大的Step拆成很多个，也可以在一个Step中实现，完全看开发者的意愿。

所有的批处理都可以描述为最简单的过程：读取大量数据，执行自定义的计算或者转换，写出处理结果，SpringBatch提供了三个主要接口来执行大量数据的读取、处理与写出：ItemReader、ItemProcessor、ItemWriter。
![Screenshot 2017-11-28 17.27.33.png](http://upload-images.jianshu.io/upload_images/4720632-ee4c1c5c0f546f2b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


**ItemReader**

ItemReader就是一种从各个数据源读取数据，然后提供给后续步骤 使用的接口，目前SpringBatch已经给我们实现了3种常用格式的处理：

1）Flat平面纯文本处理，FlatFileItemReader类实现了从纯文本文件中读取一行数据，目前支持三种格式处理：定长字符串处理、分隔符字符串处理、正则表达式字符串处理，这三种处理基本能够满足我们常见需求了，而且常见的批量数据也都是格式化的纯文本；

2）XML，XMLItemReader类提供了解析、验证、映射数据的功能，能够对XML进行处理，同时可以根据XSD schema验证格式信息是否正确；

3）数据库，SQLItemReader类实现了通过JDBC查询出数据集，然后进行数据处理；

如果上面提供的三种都不能满足要求，还可以自己去实现IteamReader接口，来完成从字符串到实体对象的转换：

```java
package org.springframework.batch.item;

public interface ItemReader<T> {
    T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException;
}
```



**ItemWriter**

ItemWriter功能上类似ItemReader的反向操作，资源任需要定位、打开和关闭，区别ItemWriter执行的是写入操作，而不是读取；

框架同样也实现了类似Reader的常用Writer类，FlatFileItemWriter、XMLItemWriter、SQLItemWriter，具体使用方法可以参考JavaDoc这里就不一一详解了。如果这几个常用Writer类满足不了你的需求那么你也可以继承ItemWriter自己去实现Writer类：

```java
package org.springframework.batch.item;

import java.util.List;

public interface ItemWriter<T> {
    void write(List<? extends T> var1) throws Exception;
}
```

自定义Writer类的话需要注意下，这里需要实现的write方法，入参是一个范型list，而ItemReader的read方法是处理一条字符串，返回一个范型对象。

**ItemProcessor**

ItemProcessor顾名思义就是数据的处理类，这个类系统没有实现类，因为是否需要对数据进行处理，对数据如何处理都是开发者自己来决定的，所以这里框架只是提供了接口，让大家去实现ItemProcessor接口中的process方法。

```java
package org.springframework.batch.item;

public interface ItemProcessor<I, O> {
    O process(I var1) throws Exception;
}
```

从接口我们可以看到ItemProcessor是一个双范型接口，需要设置输入和输出类型，第一个类型为我们ItemReader的输出类型，第二个类型为ItemWriter的输入类型也就是process方法按照开发者的意愿处理后输出的类型。

## 3. Spring Batch批量加载支付宝账单

上面我们大概说了下SpringBatch用到的架构和主要的类，因为框架的api还是比较多的，全部介绍一遍估计大家也不愿意看，下面我们直接来实战，实现一个功能，大家就会很快上手SpringBatch了，这里我们实现到指定目录读取支付宝和微信的账单并处理后输出到指定的文件，这里我们没有使用数据库，如果要使用数据库只需要设置writer里的datasource并使用SQL相关的writer类就可以了，这里不能贴出生产代码所以只是个简单的demo，整个例子并不能直接用到生产，这里仅仅是举个例子让大家能够熟悉SpringBatch，实现的功能还远远达不到生产的要求。

**实体类AlipayTranDO**

定义的阿里账单实体类，省略了setter、getter方法。

```java
public class AlipayTranDO {
	private String tranId;
	private String channel;
	private String tranType;
	private String counterparty;
	private String goods;
	private String amount;
	private String isDebitCredit;
	private String state;
...
}
```

**实体类HopPayTranDO**

需要转化的目标账单记录类。

```java
public class HopPayTranDO {
	private String tranId;
	private String channel;
	private String tranType;
	private String counterparty;
	private String goods;
	private String amount;
	private String isDebitCredit;
	private String state;
	private String tranDate;
	private String merId;
  ...
}
```

**支付宝账单Reader类**

Reader类中核心实现是通过FlatFileItemReader来处理支付宝账单单条文本记录，csv的文本记录包含了8个字段，分别通过逗号分隔，然后与AlipayTranDO类属性字段映射，必须保证同名；通过setLineTokenizer（）设置行分割方式和分割字段；通过setFieldSetMapper（）方法设置读取字段映射的类为AlipayTranDO.class，整体过程还是比较简单的；

在Reader类中还有一个getMultiAliReader()方法，该方法是获取多个文件作为Resource，让上面定义的FlatFileItemReader<AlipayTranDO>去对每个文件的每条记录单独处理，网上绝大多数的例子都是只处理一个文件，实际使用过程中不可能只处理一个批量文件，所以例子中我引入了MultiResourceItemReader类，该类是SpringBatch中用于处理多文件的情况，通过给MultiResourceItemReader设置Resource数组，并通过setDelegate设置处理单文件单Item的类实例，最后将该多文件读取的ItemReader配置在Job中即可实现多文件的读取功能。

```java
public class AlipayFileItemReader {

	private FlatFileItemReader<AlipayTranDO> reader;

	public FlatFileItemReader<AlipayTranDO> getAlipayFileItemReader() {
		reader = new FlatFileItemReader<AlipayTranDO>();
		reader.setLineMapper(new DefaultLineMapper<AlipayTranDO>() {{
			setLineTokenizer(new DelimitedLineTokenizer() {{
				setNames(new String[] { "tranId", "channel", "tranType", "counterparty", "goods", "amount", "isDebitCredit", "state" });
			}});
			setFieldSetMapper(new BeanWrapperFieldSetMapper<AlipayTranDO>() {{
				setTargetType(AlipayTranDO.class);
			}});
		}});
		reader.setLinesToSkip(5);
		return reader;
	}

	public MultiResourceItemReader<AlipayTranDO> getMultiAliReader() {
		//TODO: 获取所有当天待加载的支付宝账单,
		// 这里只是简单的放了两个csv账单文件，
		// 实际处理过程中，肯定是从数据库或者接口获取需要加载的账单文件路径
		MultiResourceItemReader<AlipayTranDO> reader = new MultiResourceItemReader<AlipayTranDO>();
		Resource[] files = new Resource[]{new FileSystemResource("data/alipay/208012345_20141030.csv"),
				new FileSystemResource("data/alipay/208054321_20141030.csv")};
		reader.setResources(files);
		reader.setDelegate(this.getAlipayFileItemReader());

		return reader;
	}
```

**AlipayItemProcessor支付宝账单处理转换类**

因为是例子，所以processor的处理过程比较简单，只是将从支付宝账单csv中读取的字段赋值给自己定义的内部聚合账单实体类，并多加工了两个字段，在实际的使用过程中大家可以按照自己系统批量加工的需求去处理加工字段；

```java
public class AlipayItemProcessor implements ItemProcessor<AlipayTranDO, HopPayTranDO> {

	private static final Logger log = LoggerFactory.getLogger(AlipayItemProcessor.class);

	@Override
	public HopPayTranDO process(AlipayTranDO alipayTranDO) throws Exception {
		HopPayTranDO hopPayTranDO = new HopPayTranDO();
		hopPayTranDO.setTranId(alipayTranDO.getTranId());
		hopPayTranDO.setChannel(alipayTranDO.getChannel());
		hopPayTranDO.setTranType(alipayTranDO.getTranType());
		hopPayTranDO.setCounterparty(alipayTranDO.getCounterparty());
		hopPayTranDO.setGoods(alipayTranDO.getGoods());
		hopPayTranDO.setAmount(alipayTranDO.getAmount());
		hopPayTranDO.setIsDebitCredit(alipayTranDO.getIsDebitCredit());
		hopPayTranDO.setState(alipayTranDO.getState());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateNowStr = sdf.format(new Date());
		hopPayTranDO.setTranDate(dateNowStr);
		hopPayTranDO.setMerId("00000001");
		log.info(alipayTranDO.toString());
		return hopPayTranDO;
	}
}
```

**AlipayFileItemWriter账单写入类**

该文件写入类就是将刚才Processor输出的加工后的HopPayTranDO对象列表写入到文件中，代码一看就明白，不用太多解释，不明白的可以留言。

```java
public class AlipayFileItemWriter {

	public FlatFileItemWriter<HopPayTranDO> getAlipayItemWriter() {
		FlatFileItemWriter<HopPayTranDO> txtItemWriter = new FlatFileItemWriter<HopPayTranDO>();
		txtItemWriter.setAppendAllowed(true);
		txtItemWriter.setShouldDeleteIfExists(true);
		txtItemWriter.setEncoding("UTF-8");
		txtItemWriter.setResource(new FileSystemResource("data/sample-data.txt"));
		txtItemWriter.setLineAggregator(new DelimitedLineAggregator<HopPayTranDO>() {{
			setDelimiter(",");
			setFieldExtractor(new BeanWrapperFieldExtractor<HopPayTranDO>() {{
				setNames(new String[]{"tranId", "channel", "tranType", "counterparty", "goods", "amount", "isDebitCredit", "state", "tranDate", "merId" });
			}});
		}});
		return txtItemWriter;
	}
}
```

**BillBatchConfig JOB配置类**

Job配置比较简单，先将下面要用到的JobBuilderFactory、StepBuilderFactory、AlipayFileItemReader、AlipayItemProcessor、AlipayFileItemWriter先分别注入实例，再配置step步骤，如果有多个步骤可以在flow(step1)后面`.next(step2)`，对于简单的批量这样的线性设置步骤就可以满足要求了，如果涉及到复杂情况，我会再写一篇来介绍SpringBatch的高级应用来讲解，本篇就不赘述了。

```java
@Configuration
@EnableBatchProcessing
public class BillBatchConfig {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private AlipayFileItemReader alipayFileItemReader;

	@Autowired
	private AlipayItemProcessor alipayItemProcessor;

	@Autowired
	private AlipayFileItemWriter alipayFileItemWriter;

	@Bean
	public Job importAliJob() {
		return jobBuilderFactory.get("importAliJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<AlipayTranDO, HopPayTranDO> chunk(10)
				.reader(alipayFileItemReader.getMultiAliReader())
				.processor(alipayItemProcessor)
				.writer(alipayFileItemWriter.getAlipayItemWriter())
				.build();
	}

}
```



## 小结

本篇文章只是SpringBatch的一个引子，还有很多功能在这里没有详细阐述，例如在批量中涉及到的为了提高性能的并发任务执行、数据校验、失败重试、记录处理异常处理等。接下来会再写一篇关于Springbatch的高级应用，里面会把上面提到的一些高级用法进行详细介绍，下面奉上前面功能的代码。

github代码：https://github.com/feiweiwei/BillSpringBatch




