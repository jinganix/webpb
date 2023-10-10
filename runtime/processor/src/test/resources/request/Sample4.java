import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class Sample4 {

    @WebpbRequestMapping(message = FooRequest.class)
    void sample(FooRequest request) {
    }
}
