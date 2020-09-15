package cn.horncomb.framework.data.mybatis.plus.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SpringPage<T> extends PageRequest implements IPage<T> {


    private static final long serialVersionUID = 8545996863226528798L;

    /**
     * 总数
     */
    private long total = 0;
    /**
     * 每页显示条数，默认 10
     */
    private long size = 10;
    /**
     * 当前页
     */
    private long current = 1;

    /**
     * 查询数据列表
     */
    private List<T> records = Collections.emptyList();

    /**
     * 自动优化 COUNT SQL
     */
    private boolean optimizeCountSql = true;
    /**
     * 是否进行 count 查询
     */
    private boolean isSearchCount = true;

    public SpringPage(Pageable pageable) {
        this(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    }

    @Deprecated
    public SpringPage(int page, int size) {
        super(page, size);
        this.setCurrent(page);
        this.setSize(size);
    }

    @Deprecated
    public SpringPage(int page, int size, Sort.Direction direction, String... properties) {
        super(page, size, direction, properties);
        this.setCurrent(page);
        this.setSize(size);
    }

    @Deprecated
    public SpringPage(int page, int size, Sort sort) {
        super(page, size, sort);
        this.setCurrent(page);
        this.setSize(size);
    }

    /**
     * Creates a new unsorted {@link PageRequest}.
     *
     * @param page zero-based page index.
     * @param size the size of the page to be returned.
     */
    public static SpringPage of(int page, int size) {
        return of(page, size, Sort.unsorted());
    }

    /**
     * Creates a new {@link PageRequest} with sort parameters applied.
     *
     * @param page zero-based page index.
     * @param size the size of the page to be returned.
     * @param sort must not be {@literal null}.
     */
    public static SpringPage of(int page, int size, Sort sort) {
        return new SpringPage(page, size, sort);
    }

    /**
     * Creates a new {@link PageRequest} with sort direction and properties applied.
     *
     * @param page       zero-based page index.
     * @param size       the size of the page to be returned.
     * @param direction  must not be {@literal null}.
     * @param properties must not be {@literal null}.
     */
    public static SpringPage of(int page, int size, Sort.Direction direction, String... properties) {
        return of(page, size, Sort.by(direction, properties));
    }

    @Override
    public String[] descs() {
        throw new IllegalStateException("This method is not used!");
    }

    @Override
    public String[] ascs() {
        throw new IllegalStateException("This method is not used!");
    }

    @Override
    public List<OrderItem> orders() {
        return null;
    }

    @Override
    public Map<Object, Object> condition() {
        return null;
    }

    @Override
    public boolean optimizeCountSql() {
        return true;
    }

    @Override
    public boolean isSearchCount() {
        if (this.getTotal() < 0) {
            return false;
        }
        return isSearchCount;
    }

    @Override
    public long offset() {
        return this.getOffset();
    }

    @Override
    public long getPages() {
        if (getSize() == 0) {
            return 0L;
        }
        long pages = getTotal() / getSize();
        if (getTotal() % getSize() != 0) {
            pages++;
        }
        return pages;
    }

    @Override
    public IPage<T> setPages(long pages) {
        // to do nothing
        return this;
    }

    @Override
    public List<T> getRecords() {
        return this.records;
    }

    @Override
    public IPage<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    @Override
    public long getTotal() {
        return this.total;
    }

    @Override
    public IPage<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public IPage<T> setSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    public long getCurrent() {
        return current;
    }

    @Override
    public IPage<T> setCurrent(long current) {
        this.current = current;
        return this;
    }
}
