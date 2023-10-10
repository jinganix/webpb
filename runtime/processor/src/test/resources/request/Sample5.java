import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class Sample5 {

    @WebpbRequestMapping
    void sample(int id, FooRequest request, String name) {
    }
}
