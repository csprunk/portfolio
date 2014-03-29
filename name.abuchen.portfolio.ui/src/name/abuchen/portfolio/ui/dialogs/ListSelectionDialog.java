package name.abuchen.portfolio.ui.dialogs;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class ListSelectionDialog extends Dialog
{
    public class ElementFilter extends ViewerFilter
    {
        private Pattern filterPattern;

        public void setSearchPattern(String pattern)
        {
            if (pattern != null)
                filterPattern = Pattern.compile(".*" + pattern + ".*", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$ //$NON-NLS-2$
            else
                filterPattern = null;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element)
        {
            if (filterPattern == null)
                return true;

            String text = labelProvider.getText(element);
            if (text == null)
                return false;
            return filterPattern.matcher(text).matches();
        }
    }

    private LabelProvider labelProvider;

    private String title;
    private String message = ""; //$NON-NLS-1$

    private Object[] elements;
    private Object[] selected;

    private TableViewer tableViewer;
    private ElementFilter elementFilter;
    private Text searchText;

    public ListSelectionDialog(Shell parentShell, LabelProvider labelProvider)
    {
        super(parentShell);
        this.labelProvider = labelProvider;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setElements(List<?> elements)
    {
        this.elements = elements.toArray();
    }

    public Object[] getResult()
    {
        return selected;
    }

    @Override
    protected void setShellStyle(int newShellStyle)
    {
        super.setShellStyle(newShellStyle | SWT.RESIZE);
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Control contents = super.createContents(parent);
        getShell().setText(title);
        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite container = new Composite(composite, SWT.None);
        GridDataFactory.fillDefaults().grab(true, true).hint(400, 300).applyTo(container);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);

        Label label = new Label(container, SWT.None);
        label.setText(this.message);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

        searchText = new Text(container, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(searchText);
        searchText.setFocus();

        Composite tableArea = new Composite(container, SWT.NONE);
        GridDataFactory.fillDefaults().grab(false, true).applyTo(tableArea);
        tableArea.setLayout(new FillLayout());

        TableColumnLayout layout = new TableColumnLayout();
        tableArea.setLayout(layout);

        elementFilter = new ElementFilter();

        tableViewer = new TableViewer(tableArea, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        final Table table = tableViewer.getTable();
        table.setHeaderVisible(false);
        table.setLinesVisible(false);

        TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.None);
        layout.setColumnData(column.getColumn(), new ColumnWeightData(100));

        tableViewer.setLabelProvider(labelProvider);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        tableViewer.addFilter(elementFilter);
        tableViewer.setInput(elements);

        tableViewer.setComparator(new ViewerComparator());

        hookListener();

        return composite;
    }

    private void hookListener()
    {
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            @Override
            public void selectionChanged(SelectionChangedEvent event)
            {
                selected = ((IStructuredSelection) event.getSelection()).toArray();
            }
        });

        tableViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            @Override
            public void doubleClick(DoubleClickEvent event)
            {
                selected = ((IStructuredSelection) event.getSelection()).toArray();
                okPressed();
            }
        });

        searchText.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                String pattern = searchText.getText().trim();
                if (pattern.length() == 0)
                {
                    elementFilter.setSearchPattern(null);
                    tableViewer.refresh();
                }
                else
                {
                    elementFilter.setSearchPattern(pattern);
                    tableViewer.refresh();
                }
            }
        });
    }
}
