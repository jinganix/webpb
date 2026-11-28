import io.github.jinganix.webpb.processor.model.BarRequest;
import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.messaging.WebpbMessageMapping;

public class SampleFailed2 {

    @WebpbMessageMapping
    void sample(FooRequest a, BarRequest b) {
    }
}
