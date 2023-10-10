package io.github.jinganix.webpb.sample.backend;

import io.github.jinganix.webpb.runtime.reactive.WebpbClient;
import io.github.jinganix.webpb.sample.proto.common.PageablePb;
import io.github.jinganix.webpb.sample.proto.common.PagingPb;
import io.github.jinganix.webpb.sample.proto.store.StoreGreetingRequest;
import io.github.jinganix.webpb.sample.proto.store.StoreGreetingResponse;
import io.github.jinganix.webpb.sample.proto.store.StoreListRequest;
import io.github.jinganix.webpb.sample.proto.store.StoreListResponse;
import io.github.jinganix.webpb.sample.proto.store.StorePb;
import io.github.jinganix.webpb.sample.proto.store.StoreVisitRequest;
import io.github.jinganix.webpb.sample.proto.store.StoreVisitResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Store service. */
@Service
@RequiredArgsConstructor
public class StoreService {

  private final WebpbClient webpbClient;

  /**
   * Request a store data.
   *
   * @param request {@link StoreVisitRequest}
   * @return {@link StoreVisitResponse}
   */
  public StoreVisitResponse getStore(StoreVisitRequest request) {
    Long id = request.getId();
    StoreGreetingResponse response =
        this.webpbClient.request(
            new StoreGreetingRequest(request.getCustomer()), StoreGreetingResponse.class);
    return new StoreVisitResponse(
        new StorePb(id, "store-" + id, "Chengdu"), response.getGreeting());
  }

  /**
   * Request a list of stores.
   *
   * @param request {@link StoreListRequest}
   * @return {@link StoreListResponse}
   */
  public StoreListResponse getStores(StoreListRequest request) {
    PageablePb pageablePb = request.getPageable();
    PagingPb pagingPb = pagingPb(pageablePb);
    List<StorePb> stores = randomStores(pagingPb);
    return new StoreListResponse(pagingPb(pageablePb), stores);
  }

  /**
   * Request a greeting message.
   *
   * @param request {@link StoreGreetingRequest}
   * @return {@link StoreGreetingResponse}
   */
  public StoreGreetingResponse greeting(StoreGreetingRequest request) {
    return new StoreGreetingResponse("Welcome, " + request.getCustomer());
  }

  private PagingPb pagingPb(PageablePb pageablePb) {
    pageablePb = pageablePb == null ? new PageablePb() : pageablePb;
    int size = pageablePb.getSize() == null ? 10 : pageablePb.getSize();
    int page = pageablePb.getPage() == null ? 1 : pageablePb.getPage();
    int totalCount = ThreadLocalRandom.current().nextInt(100, 200);
    int totalPage = (totalCount + size - 1) / size;
    return new PagingPb(page, size, totalCount, totalPage);
  }

  private List<StorePb> randomStores(PagingPb pb) {
    int size = pb.getSize();
    int page = Math.min(pb.getPage(), pb.getTotalPage());
    List<StorePb> stores = new ArrayList<>();
    long from = (long) (page - 1) * size;
    long end = Math.min(page * size, pb.getTotalCount());
    for (long id = from; id < end; id++) {
      stores.add(new StorePb(id, "store-" + id, "Chengdu"));
    }
    return stores;
  }
}
