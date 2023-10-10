import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;

public class Sample5 {

    @WebpbMessageMapping
    void sample(int id, FooRequest request, String name) {
    }
}
