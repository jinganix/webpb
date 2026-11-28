import io.github.jinganix.webpb.processor.model.BarRequest;
import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class SampleFailed3 {

    @WebpbRequestMapping
    void sample(FooRequest a, BarRequest b) {
    }
}
