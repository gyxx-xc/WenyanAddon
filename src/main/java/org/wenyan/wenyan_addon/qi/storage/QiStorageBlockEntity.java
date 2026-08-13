package org.wenyan.wenyan_addon.qi.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * 灵气池方块实体：长时间驻扎使用的灵气容器（属性 id → 储量）。
 */
public class QiStorageBlockEntity extends BlockEntity implements QiContainer {
    private final Map<String, Double> reserves = new HashMap<>();

    public QiStorageBlockEntity(BlockPos pos, BlockState blockState) {
        super(WenyanAddon.QI_STORAGE_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public double get(ElementAttribute element) {
        return reserves.getOrDefault(element.id(), 0.0);
    }

    @Override
    public double consume(ElementAttribute element, double amount) {
        double current = get(element);
        double removed = Math.min(amount, current);
        if (removed > 0) {
            reserves.put(element.id(), current - removed);
            setChanged();
        }
        return removed;
    }

    @Override
    public double add(ElementAttribute element, double amount) {
        double current = get(element);
        double added = Math.min(amount, CAPACITY - current);
        if (added > 0) {
            reserves.put(element.id(), current + added);
            setChanged();
        }
        return added;
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        net.minecraft.world.level.storage.ValueOutput qi = output.child("Qi");
        for (Map.Entry<String, Double> entry : reserves.entrySet()) {
            qi.putDouble(entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        input.child("Qi").ifPresent(reader -> {
            reserves.clear();
            for (String key : reader.keySet()) {
                reserves.put(key, reader.getDoubleOr(key, 0.0));
            }
        });
    }
}
