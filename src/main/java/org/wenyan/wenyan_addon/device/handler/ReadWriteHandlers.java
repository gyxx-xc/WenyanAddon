package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.utils.WenyanValues;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author qq240
 * @version 1.0
 * @className ReadWriteHandlers
 * @Description TODO
 * @date 2026/8/5 17:04
 */
public class ReadWriteHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> READ_WRITE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("读取文字，告示牌或讲台")
            .handler(ChineseUtils.bracketOf("读"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanDouble x=request.args().get(0).as(WenyanDouble.TYPE);
                WenyanDouble y=request.args().get(1).as(WenyanDouble.TYPE);
                WenyanDouble z=request.args().get(2).as(WenyanDouble.TYPE);
                BlockPos blockPos=new BlockPos((int) x.value(), (int) y.value(), (int) z.value());
                if (!ctx.level().isLoaded(blockPos)) {
                    throw new WenyanException.WenyanCheckerError(Component.translatable("error.wenyan_extra.no_loading").getString());
                }
                BlockEntity blockEntity = ctx.level().getBlockEntity(blockPos);
                if (blockEntity instanceof SignBlockEntity sign) {
                    WenyanList list=new WenyanList();
                    // 获取前端文本（玩家看到的）
                    for (int i = 0; i < 4; i++) {
                        Component frontText = sign.getFrontText().getMessage(i, false);
                        String text = frontText.getString();
                        // 处理文本
                        list.add(WenyanValues.of(text));
                    }
                    return list;
                } else if (blockEntity instanceof LecternBlockEntity lectern) {
                    // 获取讲台上的书
                    ItemStack book = lectern.getBook();

                    if (!book.isEmpty()) {
                        WenyanList list=new WenyanList();
                        DataComponentMap components = book.getComponents();
                        WrittenBookContent writtenBookContent = components.get(DataComponents.WRITTEN_BOOK_CONTENT);
                        WritableBookContent writableBookContent = components.get(DataComponents.WRITABLE_BOOK_CONTENT);
                        if (writableBookContent!= null) {
                            //处理书与笔
                            List<Filterable<String>> pages = writableBookContent.pages();
                            for (Filterable<String> page : pages) {
                                String s = page.get(false);
                                list.add(WenyanValues.of(s));
                            }
                        } else if (writtenBookContent != null) {
                            //处理成书
                            List<Filterable<Component>> pages = writtenBookContent.pages();
                            for (Filterable<Component> page : pages) {
                                String s = page.get(false).getString();
                                list.add(WenyanValues.of(s));
                            }
                        }
                        return list;
                    }
                }
                throw new WenyanException.WenyanDataException(Component.translatable("error.wenyan_extra.no_read").getString());
            }))
            .description("写入文字，告示牌或讲台")
            .handler(ChineseUtils.bracketOf("写"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanDouble x=request.args().get(0).as(WenyanDouble.TYPE);
                WenyanDouble y=request.args().get(1).as(WenyanDouble.TYPE);
                WenyanDouble z=request.args().get(2).as(WenyanDouble.TYPE);
                WenyanList list=request.args().get(3).as(WenyanList.TYPE);

                BlockPos blockPos=new BlockPos((int) x.value(),(int) y.value(),(int) z.value());
                if (!ctx.level().isLoaded(blockPos)) {
                    throw new WenyanException.WenyanCheckerError(Component.translatable("error.wenyan_extra.no_loading").getString());
                }
                BlockEntity blockEntity = ctx.level().getBlockEntity(blockPos);
                if (blockEntity instanceof LecternBlockEntity lectern) {
                    ItemStack book = lectern.getBook();
                    if (!book.isEmpty()) {
                        if (book.is(Items.WRITABLE_BOOK)) {
                            WritableBookContent writableBookContent = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
                            List<Filterable<String>> newPages = new ArrayList<>(); // 这里填入要写入的新内容
                            for (IWenyanValue string:list.value()){
                                String value = string.as(WenyanString.TYPE).value();
                                if (value.length()>256){
                                    value = value.substring(0,256);
                                }
                                newPages.add(Filterable.passThrough(value));
                            }
                            WritableBookContent newBook = writableBookContent.withReplacedPages(newPages);// 设置新的内容

                            book.set(DataComponents.WRITABLE_BOOK_CONTENT, newBook); // 更新书的内容
                            lectern.setChanged();
                            return WenyanValues.of(true);
                        }
                    }
                } else if (blockEntity instanceof SignBlockEntity sign) {
                    for (int i = 0; i < list.value().size(); i++) {
                        if (i==4){
                            break;
                        }
                        String value = list.value().get(i).as(WenyanString.TYPE).value();
                        if (value.length()>20){
                            value = value.substring(0,20);
                        }
                        SignText text = sign.getText(true).setMessage(i, Component.nullToEmpty(value));
                        sign.setText(text,true);
                    }
                    sign.setChanged();
                    return WenyanValues.of(true);
                }
                throw new WenyanException.WenyanCheckerError(Component.translatable("error.wenyan_extra.cant_write").getString());
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_READ_WRITE_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("读取文字，告示牌或讲台")
            .handler(ChineseUtils.bracketOf("读"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    WenyanDouble x = args.get(0).as(WenyanDouble.TYPE);
                    WenyanDouble y = args.get(1).as(WenyanDouble.TYPE);
                    WenyanDouble z = args.get(2).as(WenyanDouble.TYPE);
                    BlockPos blockPos = new BlockPos((int) x.value(), (int) y.value(), (int) z.value());
                    if (!entity.level().isLoaded(blockPos)) {
                        throw new WenyanException.WenyanCheckerError(Component.translatable("error.wenyan_extra.no_loading").getString());
                    }
                    BlockEntity blockEntity = entity.level().getBlockEntity(blockPos);
                    if (blockEntity instanceof SignBlockEntity sign) {
                        WenyanList list = new WenyanList();
                        for (int i = 0; i < 4; i++) {
                            Component frontText = sign.getFrontText().getMessage(i, false);
                            list.add(WenyanValues.of(frontText.getString()));
                        }
                        return list;
                    } else if (blockEntity instanceof LecternBlockEntity lectern) {
                        ItemStack book = lectern.getBook();
                        if (!book.isEmpty()) {
                            WenyanList list = new WenyanList();
                            DataComponentMap components = book.getComponents();
                            WrittenBookContent writtenBookContent = components.get(DataComponents.WRITTEN_BOOK_CONTENT);
                            WritableBookContent writableBookContent = components.get(DataComponents.WRITABLE_BOOK_CONTENT);
                            if (writableBookContent != null) {
                                List<Filterable<String>> pages = writableBookContent.pages();
                                for (Filterable<String> page : pages) {
                                    list.add(WenyanValues.of(page.get(false)));
                                }
                            } else if (writtenBookContent != null) {
                                List<Filterable<Component>> pages = writtenBookContent.pages();
                                for (Filterable<Component> page : pages) {
                                    list.add(WenyanValues.of(page.get(false).getString()));
                                }
                            }
                            return list;
                        }
                    }
                    throw new WenyanException.WenyanDataException(Component.translatable("error.wenyan_extra.no_read").getString());
                }
                return WenyanNull.NULL;
            })
            .description("写入文字，告示牌或讲台")
            .handler(ChineseUtils.bracketOf("写"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    WenyanDouble x = args.get(0).as(WenyanDouble.TYPE);
                    WenyanDouble y = args.get(1).as(WenyanDouble.TYPE);
                    WenyanDouble z = args.get(2).as(WenyanDouble.TYPE);
                    WenyanList list = args.get(3).as(WenyanList.TYPE);

                    BlockPos blockPos = new BlockPos((int) x.value(), (int) y.value(), (int) z.value());
                    if (!entity.level().isLoaded(blockPos)) {
                        throw new WenyanException.WenyanCheckerError(Component.translatable("error.wenyan_extra.no_loading").getString());
                    }
                    BlockEntity blockEntity = entity.level().getBlockEntity(blockPos);
                    if (blockEntity instanceof LecternBlockEntity lectern) {
                        ItemStack book = lectern.getBook();
                        if (!book.isEmpty()) {
                            if (book.is(Items.WRITABLE_BOOK)) {
                                WritableBookContent writableBookContent = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
                                List<Filterable<String>> newPages = new ArrayList<>();
                                for (IWenyanValue string : list.value()) {
                                    String value = string.as(WenyanString.TYPE).value();
                                    if (value.length() > 256) {
                                        value = value.substring(0, 256);
                                    }
                                    newPages.add(Filterable.passThrough(value));
                                }
                                WritableBookContent newBook = writableBookContent.withReplacedPages(newPages);
                                book.set(DataComponents.WRITABLE_BOOK_CONTENT, newBook);
                                lectern.setChanged();
                                return WenyanValues.of(true);
                            }
                        }
                    } else if (blockEntity instanceof SignBlockEntity sign) {
                        for (int i = 0; i < list.value().size(); i++) {
                            if (i == 4) {
                                break;
                            }
                            String value = list.value().get(i).as(WenyanString.TYPE).value();
                            if (value.length() > 20) {
                                value = value.substring(0, 20);
                            }
                            SignText text = sign.getText(true).setMessage(i, Component.nullToEmpty(value));
                            sign.setText(text, true);
                        }
                        sign.setChanged();
                        return WenyanValues.of(true);
                    }
                    throw new WenyanException.WenyanCheckerError(Component.translatable("error.wenyan_extra.cant_write").getString());
                }
                return WenyanNull.NULL;
            })
            .build();
}
