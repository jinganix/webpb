import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;

public class Sample2 {

    @WebpbMessageMapping
    void sample(FooRequest request) {
    }
}
