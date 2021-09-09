import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.impl.DefaultCamelContext;

public class ActiveMqCamel {

    public static void main(String[] args) throws Exception {

        CsvDataFormat csv = new CsvDataFormat();
        csv.setDelimiter(';');
        csv.setSkipHeaderRecord(true);

        CamelContext context = new DefaultCamelContext();

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("activemq:queue:address")
                        .process(exchange -> System.out.println("input: " + exchange.getIn().getBody()))

                        .unmarshal(csv)
                        .process(exchange -> System.out.println("unmarshal: " + exchange.getIn().getBody()))

                        .split().body()
                        .process(exchange -> System.out.println("Split body: " + exchange.getIn().getBody()))

                        .setBody(simple("insert into customer_info values(${body[0]},'${body[1]}','${body[2]}','${body[3]}','${body[4]}','${body[5]}')"))
                        .process(exchange -> System.out.println("Set body: " + exchange.getIn().getBody()))

                        .to("file:output");
            }

        });

        while(true){
            context.start();
        }

    }
}
