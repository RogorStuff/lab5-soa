package soa.eip;

import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Router extends RouteBuilder {

  public static final String DIRECT_URI = "direct:twitter";

  @Override
  public void configure() {
    from(DIRECT_URI)
      .log("Body contains \"${body}\"")
      .log("Searching twitter for \"${body}\"!")
      .choice()
        .when(body().regex(".*max:[0-9]+.*"))
          .process(sizedBody())
          .toD("twitter-search:${body}?count=${header.count}")
          .endChoice()
        .otherwise()
          .toD("twitter-search:${body}")
          .endChoice()
      .end()
      .log("Body now contains the response from twitter:\n${body}");
  }

  private Processor sizedBody() {
    return new Processor() {
      public void process(Exchange exchange) throws Exception {
        String received = exchange.getIn().getBody(String.class);
        String body = "", max = "";
        for(String part : received.split(" ")) {
          if(part.matches("max:[0-9]+")) max = part.split(":")[1];
          else body += part + " ";
        }
        exchange.getIn().setBody(body);
        exchange.getIn().setHeader("count", max);
      }
    };
  }
}